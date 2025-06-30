import psycopg2
from tabulate import tabulate


# enum for geographical granularity
geo_granularity = {
    '1': 'city',
    '2': 'region',
    '3': 'country'
}
# enum for time granularity
time_granularity = {
    '1': 'day',
    '2': 'month',
    '3': 'year'
}
# enum for product granularity
product_granularity = {
    '1': 'product_group',
    '2': 'product_family',
    '3': 'article'
}

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

def analysis(geo, time, product):
    """
    Performs analysis on the sales data based on geographical, time, and product dimensions.
    
    Args:
        geo (str): The geographical dimension to analyze.
        time (str): The time dimension to analyze.
        product (str): The product dimension to analyze.
    """
    conn = connect_to_db()
    cur = conn.cursor()

    query = f"""
        SELECT 
            {geo}, 
            {time}, 
            {product}, 
            SUM(quantity) AS total_quantity, 
            SUM(revenue) AS total_revenue
        FROM 
            star_schema.fact_sales
        JOIN 
            star_schema.dim_shop ON fact_sales.shop_id = dim_shop.id
        JOIN 
            star_schema.dim_date ON fact_sales.date_id = dim_date.id
        JOIN 
            star_schema.dim_article ON fact_sales.article_id = dim_article.id
        GROUP BY CUBE(
            {geo}, {time}, {product}
        )
        ORDER BY 
            {geo}, {time}, {product}
    """
    
    cur.execute(query)
    results = cur.fetchall()
    
    headers = [
        geo.capitalize(),
        time.capitalize(),
        product.capitalize(),
        "Total Quantity",
        "Total Revenue"
    ]
    print(tabulate(results, headers=headers, tablefmt="grid"))

    cur.close()


def drill_down():
    geo_levels = ["country", "region", "city"]
    time_levels = ["year", "month", "day", "date"]
    product_levels = ["product_family", "product_group", "article"]

    # Start at the coarsest level
    geo_index = 0
    time_index = 0
    product_index = 0

    while True:
        geo = geo_levels[geo_index]
        time = time_levels[time_index]
        product = product_levels[product_index]

        print(f"\nShowing data for Geo: {geo}, Time: {time}, Product: {product}")
        analysis(geo, time, product)

        conn = connect_to_db()
        cur = conn.cursor()

        # Build the SELECT and GROUP BY time parts dynamically
        if time == 'year':
            time_select = "EXTRACT(YEAR FROM dim_date.date) AS year"
            time_group = "EXTRACT(YEAR FROM dim_date.date)"
            time_order = "year"
        elif time == 'month':
            time_select = "EXTRACT(YEAR FROM dim_date.date) AS year, EXTRACT(MONTH FROM dim_date.date) AS month"
            time_group = "EXTRACT(YEAR FROM dim_date.date), EXTRACT(MONTH FROM dim_date.date)"
            time_order = "year, month"
        elif time == 'day':
            time_select = (
                "EXTRACT(YEAR FROM dim_date.date) AS year, "
                "EXTRACT(MONTH FROM dim_date.date) AS month, "
                "EXTRACT(DAY FROM dim_date.date) AS day"
            )
            time_group = (
                "EXTRACT(YEAR FROM dim_date.date), "
                "EXTRACT(MONTH FROM dim_date.date), "
                "EXTRACT(DAY FROM dim_date.date)"
            )
            time_order = "year, month, day"
        else:  # 'date' or exact date
            time_select = "dim_date.date"
            time_group = "dim_date.date"
            time_order = "dim_date.date"

        query = f"""
            SELECT
                dim_shop.{geo} AS region,
                {time_select},
                dim_article.{product} AS product,
                SUM(fact_sales.quantity) AS total_quantity,
                SUM(fact_sales.revenue) AS total_revenue
            FROM star_schema.fact_sales
            JOIN star_schema.dim_date ON fact_sales.date_id = dim_date.id
            JOIN star_schema.dim_shop ON fact_sales.shop_id = dim_shop.id
            JOIN star_schema.dim_article ON fact_sales.article_id = dim_article.id
            GROUP BY ROLLUP (
                dim_shop.{geo},
                {time_group},
                dim_article.{product}
            )
            ORDER BY region, {time_order}, product;
        """

        cur.execute(query)
        results = cur.fetchall()
        
        headers = [
            geo.capitalize(),
            time.capitalize(),
            product.capitalize(),
            "Total Quantity",
            "Total Revenue"
        ]
        print(tabulate(results, headers=headers, tablefmt="grid"))

        # Print results here or call a function to display

        print("\nOptions:")
        print("1. Drill down Geo")
        print("2. Roll up Geo")
        print("3. Drill down Time")
        print("4. Roll up Time")
        print("5. Drill down Product")
        print("6. Roll up Product")
        print("0. Exit")

        choice = input("Choose option: ").strip()

        if choice == "1" and geo_index + 1 < len(geo_levels):
            geo_index += 1
        elif choice == "2" and geo_index - 1 >= 0:
            geo_index -= 1
        elif choice == "3" and time_index + 1 < len(time_levels):
            time_index += 1
        elif choice == "4" and time_index - 1 >= 0:
            time_index -= 1
        elif choice == "5" and product_index + 1 < len(product_levels):
            product_index += 1
        elif choice == "6" and product_index - 1 >= 0:
            product_index -= 1
        elif choice == "0":
            print("Exiting navigation.")
            break
        else:
            print("Invalid option or no further level in that direction.")

        cur.close()
        conn.close()
        




if __name__ == "__main__":
    choice = input("Do you want to drill down (d) or analyze (a)? ").strip().lower()

    if choice == 'd':
        print("Starting drill down navigation...")
        drill_down()
    elif choice == 'a':
        print("Starting analysis...")

        # Example usage of the analysis function
        print("Starting analysis...")
        # You can change the parameters to analyze different dimensions
        # enter user input for geo where the user can select the granularity using a number
        geo = input("Enter geographical granularity:\n [1] City\n [2] Region\n [3] Country\n")
        time = input("Enter time granularity:\n [1] Day\n [2] Month\n [3] Year\n")
        product = input("Enter product granularity:\n [1] Product Group\n [2] Product Family\n [3] Article\n")

        analysis(geo_granularity[geo], time_granularity[time], product_granularity[product])