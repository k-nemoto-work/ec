# 要求内容: お気に入り管理

## 背景

カート管理（#3）よりも先に実装する。
お気に入り機能はカート機能に依存しないため、独立して実装可能。

## 対象機能

functional-design.md に定義されている「お気に入り (Favorite)」機能を実装する。

## APIエンドポイント

| メソッド | パス | 説明 | 認証 |
|---|---|---|---|
| GET | `/api/v1/favorites` | お気に入り一覧取得 | 必要 |
| POST | `/api/v1/favorites/items` | お気に入りに追加 | 必要 |
| DELETE | `/api/v1/favorites/items/{productId}` | お気に入りから削除 | 必要 |

## ドメインモデル

```kotlin
data class Favorite(
    val id: FavoriteId,
    val customerId: CustomerId,
    val items: List<FavoriteItem>
)

data class FavoriteItem(
    val productId: ProductId,
    val addedAt: Instant
)
```

## ビジネスルール

1. 同じ商品の重複追加不可（`FAVORITE_ITEM_ALREADY_EXISTS`）
2. 追加できる商品ステータス: `ON_SALE` または `RESERVED` のみ
3. `SOLD`・`PRIVATE` の商品は追加不可（`PRODUCT_NOT_AVAILABLE_FOR_FAVORITE`）
4. 存在しない商品は追加不可（`PRODUCT_NOT_FOUND`）
5. 他の顧客のお気に入りにはアクセス不可（403）

## 除外範囲

- カート管理との連携（未実装のため）
- お気に入りからカートへの追加
