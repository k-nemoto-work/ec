CREATE TABLE favorites (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL REFERENCES customers(id)
);

CREATE UNIQUE INDEX idx_favorites_customer_id ON favorites(customer_id);

-- product_id は products(id) への外部キーを意図的に設けない。
-- 商品がカタログから削除された後もお気に入り登録履歴を保持するため。
-- アプリ側 (GetFavoriteUseCase) で存在しない商品を無言でスキップする。
CREATE TABLE favorite_items (
    favorite_id UUID NOT NULL REFERENCES favorites(id),
    product_id UUID NOT NULL,
    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (favorite_id, product_id)
);
