CREATE SCHEMA IF NOT EXISTS star_schema;

CREATE TABLE star_schema.fact_sales (
    id SERIAL PRIMARY KEY,
    date_id INT NOT NULL,
    shop_id INT NOT NULL,
    article_id INT NOT NULL,
    quantity INT NOT NULL,
    revenue DOUBLE precision NOT NULL
);

CREATE TABLE star_schema.dim_date (
    id SERIAL PRIMARY KEY,
    date DATE NOT NULL,
    year INT NOT NULL,
    month INT NOT NULL,
    day INT NOT NULL,
    UNIQUE (date, day, month, year) -- Ensure uniqueness of date components
);

CREATE TABLE star_schema.dim_shop (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    city VARCHAR(255) NOT NULL,
    country VARCHAR(255) NOT NULL,
    region VARCHAR(255) NOT NULL
);

CREATE TABLE star_schema.dim_article (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DOUBLE precision NOT NULL,
    product_group VARCHAR(255) NOT NULL,
    product_family VARCHAR(255) NOT NULL,
    product_category VARCHAR(255) NOT NULL
);

ALTER TABLE star_schema.fact_sales ADD CONSTRAINT date_fk_1 FOREIGN KEY (date_id) REFERENCES star_schema.dim_date (id);
ALTER TABLE star_schema.fact_sales ADD CONSTRAINT shop_fk_1 FOREIGN KEY (shop_id) REFERENCES star_schema.dim_shop (id);
ALTER TABLE star_schema.fact_sales ADD CONSTRAINT article_fk_1 FOREIGN KEY (article_id) REFERENCES star_schema.dim_article (id);

CREATE INDEX IF NOT EXISTS idx_article_name ON star_schema.dim_article(name);
CREATE INDEX IF NOT EXISTS idx_shop_name ON star_schema.dim_shop(name);
CREATE INDEX IF NOT EXISTS idx_date_full ON star_schema.dim_date(day, month, year);