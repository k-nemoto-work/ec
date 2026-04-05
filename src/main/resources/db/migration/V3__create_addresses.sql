CREATE TABLE addresses (
    customer_id UUID PRIMARY KEY REFERENCES customers(id),
    postal_code VARCHAR(10) NOT NULL,
    prefecture VARCHAR(10) NOT NULL,
    city VARCHAR(50) NOT NULL,
    street_address VARCHAR(200) NOT NULL
);
