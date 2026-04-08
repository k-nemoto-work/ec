CREATE TABLE favorites (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL REFERENCES customers(id)
);

CREATE UNIQUE INDEX idx_favorites_customer_id ON favorites(customer_id);

CREATE TABLE favorite_items (
    favorite_id UUID NOT NULL REFERENCES favorites(id),
    product_id UUID NOT NULL,
    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (favorite_id, product_id)
);
