# タスクリスト

## 🚨 タスク完全完了の原則

**このファイルの全タスクが完了するまで作業を継続すること**

---

## フェーズ1: コード修正

- [x] `OrderStatus.kt` から `PENDING` と TODO コメントを削除
- [x] `Order.kt` の修正
  - [x] `Order.create()` 初期ステータスを `CONFIRMED` に変更
  - [x] KDoc コメント「PENDING または CONFIRMED」→「CONFIRMED」に修正

## フェーズ2: テスト修正

- [x] `OrderTest.kt` の修正
  - [x] デフォルト引数 `OrderStatus.PENDING` → `OrderStatus.CONFIRMED`
  - [x] `when` ブランチの `PENDING -> order` を削除
  - [x] `assertEquals(OrderStatus.PENDING, ...)` → `OrderStatus.CONFIRMED`
  - [x] テスト名「PENDING状態」→「CONFIRMED状態」（PENDING テストを削除、CONFIRMED テストが残る）
  - [x] `createOrder(OrderStatus.PENDING)` → 削除済み
- [x] `CancelOrderUseCaseTest.kt` の修正
  - [x] テスト名「PENDING状態」→「CONFIRMED状態」（L82）
  - [x] `createOrder(customerId, OrderStatus.PENDING)` → `CONFIRMED`（L84）
  - [x] `createOrder(otherCustomerId, OrderStatus.PENDING)` → `CONFIRMED`（L114）
- [x] `UpdateShipmentUseCaseTest.kt` の修正
  - [x] `status = OrderStatus.PENDING` → `OrderStatus.CONFIRMED`（L63）
- [x] `UpdatePaymentUseCaseTest.kt` の修正
  - [x] `status = OrderStatus.PENDING` → `OrderStatus.CONFIRMED`（L53）

## フェーズ3: ドキュメント修正

- [x] `docs/functional-design.md` の修正
  - [x] `Customer.name: String` → `val name: CustomerName` に修正（L76 付近）
  - [x] `CartItem` に `addedAt: Instant` フィールドを追記（L178-180 付近）
  - [x] OrderStatus から `PENDING` を削除（L207-213 付近）
  - [x] ビジネスルール記述を更新（注文確定時に CONFIRMED で作成される旨を追記）
  - [x] Product API テーブルに `GET /api/v1/products/{productId}/management` を追記（L419 付近）

## フェーズ4: 検証

- [x] テスト実行
  - [x] `./gradlew test` がグリーンになることを確認

---

## 実装後の振り返り

### 実装完了日
2026-04-11

### 計画と実績の差分

**計画と異なった点**:
- `OrderTest.kt` の「PENDING状態の注文をキャンセルできる」テストは削除。「CONFIRMED状態の注文をキャンセルできる」テストが既に存在していたため、重複テストになることを避けた。

**新たに必要になったタスク**:
- なし

### 学んだこと
- `OrderStatus.CONFIRMED` は enum に定義されていたが TODO コメントで未実装が明示されていた。dead state を早期に発見・解消できた。
- ドキュメントと実装の乖離（CartItem.addedAt、CustomerName、/management エンドポイント）は実装時に気づきにくいため、定期的なレビューが有効。
