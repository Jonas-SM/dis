import pandas as pd
import psycopg2
from charset_normalizer import from_path
from datetime import datetime


def connect_to_db():    
    """
    Connects to the PostgreSQL database and returns the connection object.
    
    Returns:
        conn: A connection object to the PostgreSQL database.
    """
    conn = psycopg2.connect(
        dbname='postgres',
        user='postgres',
        password='postgres',
        host='localhost',
        port='5432'
    )
    return conn


def createSchema():
    conn = connect_to_db()
    cur = conn.cursor()

    # Read and execute the SQL file
    with open('schema.sql', 'r') as file:
        sql_script = file.read()
        cur.execute(sql_script)  # executes entire script

    # Commit and close
    conn.commit()

def addDateDimension(df, cursor):
    """
    Adds a date dimension to the database.
    
    Args:
        row (list): A list representing a row in the CSV file.
        cursor: A database cursor object to execute SQL commands.
    """
    df['parsed_date'] = pd.to_datetime(df['Date'], format='%d.%m.%Y')
    dim_date_df = df[['parsed_date']].drop_duplicates().copy()
    dim_date_df['day'] = dim_date_df['parsed_date'].dt.day
    dim_date_df['month'] = dim_date_df['parsed_date'].dt.month
    dim_date_df['year'] = dim_date_df['parsed_date'].dt.year

    # Convert numpy datetime64 to native Python date
    dim_date_df['parsed_date'] = dim_date_df['parsed_date'].dt.date
    dim_date_df = dim_date_df.astype(object)

    # Create the list of tuples
    records = list(dim_date_df.to_records(index=False))

    cursor.executemany(
        """
        INSERT INTO star_schema.dim_date (date, day, month, year)
        VALUES (%s, %s, %s, %s)
        ON CONFLICT DO NOTHING
        """,
        records
    )


def process_row(row, cursor, dim_date, dim_shop, dim_article):
    """
    Processes a single row of data from the CSV file.
    
    Args:
        row (list): A list representing a row in the CSV file.
    """
    date = row.Date
    shop = row.Shop
    article = row.Article
    quantity = row.Sold
    revenue = row.Revenue
    
    # populate the date dimension
    day = int(date.split('.')[0])
    month = int(date.split('.')[1])
    year = int(date.split('.')[2])
    date = datetime(year, month, day).date()

    date_id = -1
    shop_id = -1
    article_id = -1

    #cursor.execute(
    #    """
    #    INSERT INTO star_schema.dim_date (date, day, month, year)
    #    VALUES (%s, %s, %s, %s)
    #    ON CONFLICT (date, day, month, year) DO NOTHING
    #    """,
    #    (date, day, month, year)
    #)
    
    date_id = dim_date.get(date)
    if (not date_id):
        print(f"Date {date} not found in dimension table. Skipping row.")
        return None

    shop_id = dim_shop.get(shop)
    if (not shop_id):
        print(f"Shop {shop} not found in dimension table. Skipping row.")
        return None

    article_id = dim_article.get(article)
    if (not article_id):
        print(f"Article {article} not found in dimension table. Skipping row.")
        return None

    return ([date_id, shop_id, article_id, quantity, revenue])


def fill_dimensions():
    """
    Fills dimension tables with data from the CSV file.
    """
    conn = connect_to_db()
    cur = conn.cursor()

    cur.execute(
        """
                SELECT 
                shop.name as shop_name,
                city.name AS city_name,
                country.name AS country_name,
                region.name AS region_name
            FROM 
                shop
            join
                city on shop.cityid = city.cityid
            JOIN 
                region ON city.regionid = region.regionid
            JOIN 
                country ON region.countryid = country.countryid;
        """)    
    rows = cur.fetchall()

    insert_query = """
        INSERT INTO star_schema.dim_shop (name, city, country, region)
        VALUES (%s, %s, %s, %s)
    """

    cur.executemany(insert_query, rows)
    conn.commit()

    cur.execute("""
        SELECT 
            article.name as article_name,
            article.price as article_price,
            productgroup.name as product_group,
            productfamily.name as product_name,
            productcategory.name as product_category
        FROM 
            article
        join
            productgroup on article.productgroupid = productgroup.productgroupid 
        JOIN 
            productfamily ON productgroup.productfamilyid = productfamily.productfamilyid 
        JOIN 
            productcategory ON productfamily.productcategoryid = productcategory.productcategoryid 
    """)
    rows = cur.fetchall()
    insert_query = """
        INSERT INTO star_schema.dim_article (name, price, product_group, product_family, product_category)
        VALUES (%s, %s, %s, %s, %s)
    """
    cur.executemany(insert_query, rows)
    conn.commit()

def startETL():
    conn = connect_to_db()
    cur = conn.cursor()

    createSchema()

    fill_dimensions()

    """
    Starts the ETL process by reading data from a CSV file and processing it.
    """
    df = pd.read_csv('sales.csv', encoding='latin1', sep=';', on_bad_lines='skip', decimal=',')
    sales = []

    addDateDimension(df, cur)     

    conn.commit()   
    print("Date dimension added successfully.")

    # load dimension tables into memory
    cur.execute("SELECT * FROM star_schema.dim_date;")
    dim_date = {row[1]: row[0] for row in cur.fetchall()}
    cur.execute("SELECT * FROM star_schema.dim_shop;")
    dim_shop = {row[1]: row[0] for row in cur.fetchall()}       
    cur.execute("SELECT * FROM star_schema.dim_article;")
    dim_article = {row[1]: row[0] for row in cur.fetchall()}

    for row in df.itertuples():
        sales.append(process_row(row, cur, dim_date, dim_shop, dim_article))

    for x in sales:
        if (x is None):
            print("Skipping row due to missing dimension data.")
            sales.remove(x)
        else:
            if (any(y is None for y in x)):
                print("Skipping row due to missing dimension data.")
                sales.remove(x)

    cur.executemany(
        """
        INSERT INTO star_schema.fact_sales (date_id, shop_id, article_id, quantity, revenue)
        VALUES (%s, %s, %s, %s, %s)
        """,
        sales
    )
    
    conn.commit()

def cleanUp():
    """
    Cleans up the database by dropping the star_schema schema.
    """
    conn = connect_to_db()
    cur = conn.cursor()

    cur.execute("DROP SCHEMA IF EXISTS star_schema CASCADE;")
    conn.commit()

    cur.close()
    print("Database cleaned up successfully.")

if __name__ == "__main__":
    cleanUp()

    try:
        startETL()
    finally:
        print("ETL process completed successfully.")