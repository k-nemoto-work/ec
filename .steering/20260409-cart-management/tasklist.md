# タスクリスト - カート管理

## フェーズ1: Domain層

- [x] `CartId.kt` を作成（UUID wrapper値オブジェクト）
- [x] `CartItem.kt` を作成（productId 保持）
- [x] `Cart.kt` を作成（集約ルート: addItem / removeItem / create）
- [x] `CartRepository.kt` を作成（インターフェース: findByCustomerId / save）

## フェーズ2: Infrastructure層

- [x] `V7__create_carts.sql` を作成（carts テーブル）
- [x] `V8__create_cart_items.sql` を作成（cart_items テーブル）
- [x] `CartsTable.kt` を作成（Exposed DSL）
- [x] `CartItemsTable.kt` を作成（Exposed DSL）
- [x] `CartRepositoryImpl.kt` を作成（findByCustomerId / save 実装）

## フェーズ3: UseCase層

- [x] `AddToCartCommand.kt` を作成
- [x] `AddToCartUseCase.kt` を作成
- [x] `RemoveFromCartCommand.kt` を作成
- [x] `RemoveFromCartUseCase.kt` を作成
- [x] `CartItemResult.kt` を作成
- [x] `CartResult.kt` を作成
- [x] `GetCartUseCase.kt` を作成

## フェーズ4: HTTP層

- [x] `CartController.kt` を作成（GET /api/v1/cart, POST /items, DELETE /items/{productId}）

## フェーズ5: テスト

- [x] `CartTest.kt` を作成（Domain層ユニットテスト）
- [x] `AddToCartUseCaseTest.kt` を作成
- [x] `GetCartUseCaseTest.kt` を作成
- [x] `RemoveFromCartUseCaseTest.kt` を作成

## 実装後の振り返り

**実装完了日**: 2026-04-09

### 計画と実績の差分

- 計画通りに全タスクを完了した。
- `functional-design.md` に POST /api/v1/cart/items のレスポンスステータスの明示的な記載がなかったが、`FavoriteController` の既存パターン（204 No Content）に合わせて実装した。

### 学んだこと

- Favorite 機能の実装パターン（集約・Repository差分更新・UseCase構造・Controller）を完全に踏襲することで、コードベースの一貫性を保ちながら高速に実装できた。
- `CartRepositoryImpl` の差分更新（`currentProductIds - desiredProductIds`）は `FavoriteRepositoryImpl` と全く同じパターンで再利用可能だった。
- Domain層のビジネスルール（`addItem`/`removeItem`）を先に実装してテストすることで、UseCase層の実装が薄くシンプルに保てた。

### 次回への改善提案

- Infrastructure 統合テスト（`CartRepositoryImplTest` with TestContainers）が未実装。将来的に `FavoriteRepositoryImpl` と同様のパターンで追加が推奨される。
- HTTP 層テスト（`CartControllerTest` with MockMvc）も未実装。エンドポイントの認証あり/なし検証として追加が推奨される。
- POST `/api/v1/cart/items` のレスポンスは `204 No Content` を返しているが、`docs/functional-design.md` に明示的な記載がないため、ドキュメントを更新して一致させると良い。
