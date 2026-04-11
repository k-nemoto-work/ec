CREATE TABLE order_items (
    order_id UUID NOT NULL REFERENCES orders(id),
    product_id UUID NOT NULL,
    product_name_snapshot VARCHAR(100) NOT NULL,
    price_snapshot BIGINT NOT NULL,
    PRIMARY KEY (order_id, product_id)
);
