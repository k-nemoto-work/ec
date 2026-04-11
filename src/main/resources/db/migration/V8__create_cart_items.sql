-- product_id は products(id) への外部キーを設けない。
-- 商品がカタログから削除された後もカートデータを保持するため。
CREATE TABLE cart_items (
    cart_id UUID NOT NULL REFERENCES carts(id),
    product_id UUID NOT NULL,
    PRIMARY KEY (cart_id, product_id)
);
