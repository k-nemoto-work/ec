CREATE TABLE carts (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL REFERENCES customers(id)
);

CREATE UNIQUE INDEX idx_carts_customer_id ON carts(customer_id);
