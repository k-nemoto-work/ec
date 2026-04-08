# 実装設計: お気に入り管理

## アーキテクチャ方針

既存の商品管理・顧客管理の実装パターンに完全準拠する。
クリーンアーキテクチャ 4層構成（HTTP → UseCase → Domain ← Infrastructure）。

## ファイル構成

### Domain層
```
src/main/kotlin/com/example/ec/domain/customer/
├── Favorite.kt           # 集約ルート（新規）
├── FavoriteId.kt         # 値オブジェクト（新規）
├── FavoriteItem.kt       # エンティティ（新規）
└── FavoriteRepository.kt # Repositoryインターフェース（新規）
```

### UseCase層
```
src/main/kotlin/com/example/ec/usecase/favorite/
├── get/
│   ├── GetFavoriteUseCase.kt
│   ├── FavoriteResult.kt
│   └── FavoriteItemResult.kt
├── add/
│   ├── AddToFavoriteUseCase.kt
│   └── AddToFavoriteCommand.kt
└── remove/
    ├── RemoveFromFavoriteUseCase.kt
    └── RemoveFromFavoriteCommand.kt
```

### Infrastructure層
```
src/main/kotlin/com/example/ec/infrastructure/
├── table/
│   ├── FavoritesTable.kt      # お気に入りテーブル（新規）
│   └── FavoriteItemsTable.kt  # お気に入りアイテムテーブル（新規）
└── repository/
    └── FavoriteRepositoryImpl.kt

src/main/resources/db/migration/
└── V6__create_favorites.sql
```

### HTTP層
```
src/main/kotlin/com/example/ec/http/controller/
└── FavoriteController.kt
```

## 主要設計判断

### お気に入りの初期化
- 顧客登録時にお気に入りを自動作成 **しない**
  - lazy 初期化: 初回 GET または POST 時に存在しなければ自動作成
  - RegisterCustomerUseCase への変更を避けるため

### GET /api/v1/favorites レスポンス
- お気に入りが未作成の場合は空リストを返す（404 にしない）
- 商品情報をJOINして1クエリで返す（N+1対策）

### FavoriteRepository インターフェース
```kotlin
interface FavoriteRepository {
    fun findByCustomerId(customerId: CustomerId): Favorite?
    fun save(favorite: Favorite)
    fun update(favorite: Favorite)
}
```

### Favorite ドメインモデルのメソッド
```kotlin
// 商品追加（ビジネスルール検証を含む）
fun addItem(productId: ProductId, productStatus: ProductStatus): Favorite

// 商品削除
fun removeItem(productId: ProductId): Favorite
```

### GET レスポンス形式
```json
{
  "favoriteId": "uuid",
  "items": [
    {
      "productId": "uuid",
      "productName": "商品名",
      "price": 3000,
      "status": "ON_SALE",
      "addedAt": "2026-04-08T10:00:00Z"
    }
  ]
}
```

## エラーコード

| コード | 意味 | HTTP |
|---|---|---|
| `PRODUCT_NOT_FOUND` | 商品が存在しない | 404 |
| `PRODUCT_NOT_AVAILABLE_FOR_FAVORITE` | 追加不可ステータスの商品 | 409 |
| `FAVORITE_ITEM_ALREADY_EXISTS` | 同じ商品が既に存在する | 409 |
| `FAVORITE_ITEM_NOT_FOUND` | 削除対象の商品がお気に入りにない | 404 |
