CREATE TABLE payments (
    order_id UUID PRIMARY KEY REFERENCES orders(id),
    method VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL
);
