# 実装アプローチ - カート管理

## アーキテクチャ方針

Favorite機能の実装パターンを踏襲してカート管理を実装する。

## ファイル構成

### Domain層 (`src/main/kotlin/com/example/ec/domain/order/`)

```
CartId.kt         - 値オブジェクト（UUID wrapper）
CartItem.kt       - 値オブジェクト（productId のみ保持）
Cart.kt           - 集約ルート（ビジネスルール実装）
CartRepository.kt - Repositoryインターフェース
```

### UseCase層

```
usecase/cart/get/
  GetCartUseCase.kt       - カート取得（商品情報JOIN）
  CartResult.kt           - レスポンスDTO
  CartItemResult.kt       - 商品アイテムDTO

usecase/cart/add/
  AddToCartUseCase.kt     - カートに商品追加
  AddToCartCommand.kt     - コマンド

usecase/cart/remove/
  RemoveFromCartUseCase.kt     - カートから商品削除
  RemoveFromCartCommand.kt     - コマンド
```

### Infrastructure層

```
infrastructure/table/
  CartsTable.kt       - carts テーブルDSL
  CartItemsTable.kt   - cart_items テーブルDSL

infrastructure/repository/
  CartRepositoryImpl.kt - CartRepository の実装（Exposed DSL）
```

### HTTP層

```
http/controller/
  CartController.kt   - GET /api/v1/cart, POST /items, DELETE /items/{productId}
```

### DBマイグレーション

```
V7__create_carts.sql   - carts テーブル作成
V8__create_cart_items.sql - cart_items テーブル作成
```

## 主な設計判断

### Cart集約のビジネスルール

`Cart.addItem(product)` メソッドでビジネスルールを実装:
- 商品ステータスが `ON_SALE` でない場合: `BusinessRuleViolationException`
- 同一商品の重複追加: `BusinessRuleViolationException`

`Cart.removeItem(productId)` メソッド:
- 存在しない商品: `ResourceNotFoundException`

### GetCartUseCase

- カートが未存在の場合は空のカートを返す（favoriteと同様のパターン）
- 商品情報をJOINして `ProductRepository.findAllByIds()` で取得
- 存在しなくなった商品はnullでスキップ
- 合計金額 = ON_SALE商品の現在価格の合計（ステータスを問わず全商品の価格を合計）

### CartRepositoryImpl

- FavoriteRepositoryImpl と同様の差分更新パターン
- `findByCustomerId`, `save` の2メソッド
- cart_items の差分更新（追加・削除のみ実行）

### Flywayマイグレーション番号

既存は V6 まで存在するため V7, V8 を使用する。

## テスト方針

- `CartTest`: Domain層ユニットテスト（ビジネスルール検証）
- `AddToCartUseCaseTest`: UseCase層ユニットテスト（MockK使用）
- `GetCartUseCaseTest`: UseCase層ユニットテスト（MockK使用）
- `RemoveFromCartUseCaseTest`: UseCase層ユニットテスト（MockK使用）
