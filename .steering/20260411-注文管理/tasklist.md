# タスクリスト

## 🚨 タスク完全完了の原則

**このファイルの全タスクが完了するまで作業を継続すること**

### 必須ルール
- **全てのタスクを`[x]`にすること**
- 「時間の都合により別タスクとして実施予定」は禁止
- 「実装が複雑すぎるため後回し」は禁止
- 未完了タスク（`[ ]`）を残したまま作業を終了しない

---

## フェーズ1: Domain層

- [x] `UnauthorizedAccessException.kt` を作成
- [x] `OrderStatus.kt` を作成（PENDING / CONFIRMED / SHIPPING / DELIVERED / CANCELLED）
- [x] `PaymentMethod.kt`, `PaymentStatus.kt` を作成
- [x] `ShipmentStatus.kt` を作成
- [x] `OrderId.kt` を作成
- [x] `OrderItem.kt` を作成（productId, productNameSnapshot, priceSnapshot）
- [x] `Payment.kt` を作成（method, status）
- [x] `Shipment.kt` を作成（address, status）
- [x] `Order.kt` を作成（集約ルート + ビジネスルール）
  - [x] `Order.create()` companion object
  - [x] `Order.cancel()`: SHIPPING/DELIVERED 後はキャンセル不可
  - [x] `Order.updatePayment()`: PAID に変更
  - [x] `Order.updateShipment()`: PAID でないと SHIPPED に進められない
- [x] `OrderRepository.kt` インターフェースを作成
- [x] `GlobalExceptionHandler` に `UnauthorizedAccessException` の 403 ハンドリングを追加

## フェーズ2: Infrastructure層（DB）

- [x] `V10__create_orders.sql` を作成
- [x] `V11__create_order_items.sql` を作成
- [x] `V12__create_payments.sql` を作成
- [x] `V13__create_shipments.sql` を作成
- [x] `OrdersTable.kt` を作成
- [x] `OrderItemsTable.kt` を作成
- [x] `PaymentsTable.kt` を作成
- [x] `ShipmentsTable.kt` を作成
- [x] `OrderRepositoryImpl.kt` を作成
  - [x] `findById(orderId)`: 注文詳細取得（items/payment/shipment JOIN）
  - [x] `findByCustomerId(customerId, page, size)`: ページネーション付き一覧
  - [x] `save(order)`: upsert（orders + items 差分 + payments/shipments upsert）

## フェーズ3: UseCase層

- [x] `PlaceOrderUseCase.kt` を作成（@Transactional）
  - [x] `PlaceOrderResult.kt` を作成
- [x] `GetOrdersUseCase.kt` を作成
  - [x] `OrderSummaryResult.kt` を作成
  - [x] `OrdersResult.kt` を作成
- [x] `GetOrderUseCase.kt` を作成（他人アクセスは UnauthorizedAccessException）
  - [x] `OrderResult.kt` を作成
- [x] `CancelOrderUseCase.kt` を作成（キャンセル + 商品 ON_SALE 戻し）
- [x] `UpdatePaymentUseCase.kt` を作成
  - [x] `UpdatePaymentCommand.kt` を作成
- [x] `UpdateShipmentUseCase.kt` を作成（DELIVERED 時に商品 SOLD）
  - [x] `UpdateShipmentCommand.kt` を作成

## フェーズ4: HTTP層

- [x] `OrderController.kt` を作成
  - [x] `POST /api/v1/orders` - 注文確定
  - [x] `GET /api/v1/orders` - 注文履歴一覧
  - [x] `GET /api/v1/orders/{orderId}` - 注文詳細
  - [x] `PATCH /api/v1/orders/{orderId}/cancel` - 注文キャンセル
  - [x] `PATCH /api/v1/orders/{orderId}/payment` - 決済ステータス更新
  - [x] `PATCH /api/v1/orders/{orderId}/shipment` - 配送ステータス更新

## フェーズ5: テスト

- [x] `OrderTest.kt` を作成（Domain ユニットテスト）
  - [x] cancel(): SHIPPING/DELIVERED 後のキャンセルでエラー
  - [x] cancel(): PENDING/CONFIRMED 時は正常にキャンセルできる
  - [x] updateShipment(): UNPAID 状態で SHIPPED に進めようとするとエラー
  - [x] updateShipment(): PAID 状態で SHIPPED/DELIVERED に進められる
- [x] `PlaceOrderUseCaseTest.kt` を作成
  - [x] 正常系: カートの商品が注文になる
  - [x] カートが空の場合は BusinessRuleViolationException
  - [x] ON_SALE 以外の商品が含まれる場合は BusinessRuleViolationException
- [x] `CancelOrderUseCaseTest.kt` を作成
  - [x] 正常系: キャンセル成功
  - [x] SHIPPING 以降はキャンセルできない
- [x] `UpdatePaymentUseCaseTest.kt` を作成
  - [x] 正常系: PAID に変更できる
- [x] `UpdateShipmentUseCaseTest.kt` を作成
  - [x] 正常系: SHIPPED → DELIVERED
  - [x] DELIVERED 時に商品が SOLD になる
  - [x] UNPAID 状態で SHIPPED に進めようとするとエラー

## フェーズ6: 品質チェック

- [x] `./gradlew test` で全テストが通ることを確認
- [x] `./gradlew build` でビルドが成功することを確認

---

## 実装後の振り返り

### 実装完了日
2026-04-11

### 計画と実績の差分

**計画と異なった点**:
- `OrderRepositoryImpl` の差分更新（deleteWhere + inList + and）は Exposed の `SqlExpressionBuilder` ラムダ内で `and` が正しく解決されないコンパイルエラーが発生した。
- 代替として order_items は「全削除→再挿入」方式に変更。注文確定後はアイテムが変更されないため、意味的にも問題なし。

**新たに必要になったタスク**:
- `PlaceOrderCommand.kt` の追加（当初 UseCase に直接引数を受け取る設計だったが、既存パターンに合わせて Command クラスを追加）

**技術的理由でスキップしたタスク**: なし（全タスク完了）

### 学んだこと

**技術的な学び**:
- Exposed の `deleteWhere { and(...) }` は `SqlExpressionBuilder` レシーバのスコープ内で `Op<Boolean>.and()` が正常に解決されないケースがある。単一条件またはシンプルな表現に留め、複合条件は「全削除→再挿入」に切り替える方が安全。
- IDE の新規ファイル認識遅延: `UnauthorizedAccessException` を新規作成直後は IDE の診断エラーが出るが、コンパイルは正常に通る。

**プロセス上の改善点**:
- ステアリングファイルを計画段階で作成してからの実装は、スコープが明確で効率的だった。

### 次回への改善提案
- `OrderRepository.countByCustomerId` を追加してページネーションのトータルカウントを返すと API として完全になる。
- Infrastructure 層の統合テスト（TestContainers）は Docker 環境が必要なため、CI での実行を想定した設定確認が必要。
