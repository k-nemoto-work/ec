CREATE TABLE shipments (
    order_id UUID PRIMARY KEY REFERENCES orders(id),
    postal_code VARCHAR(8) NOT NULL,
    prefecture VARCHAR(50) NOT NULL,
    city VARCHAR(100) NOT NULL,
    street_address VARCHAR(200) NOT NULL,
    status VARCHAR(20) NOT NULL
);
