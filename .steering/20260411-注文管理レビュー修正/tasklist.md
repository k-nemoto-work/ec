# タスクリスト: 注文管理レビュー修正

## フェーズ1: Bug修正

- [x] `OrderRepositoryImpl.kt`: `paymentsByOrderId[orderId]!!` を安全な取得に変更
- [x] `OrderRepositoryImpl.kt`: `shipmentsByOrderId[orderId]!!` を安全な取得に変更
- [x] `OrderController.kt`: `updatePayment` の不要な `@RequestBody request` を削除
- [x] `GlobalExceptionHandler.kt`: `IllegalArgumentException` のハンドラーを追加

## フェーズ2: 設計上の問題

- [x] `OrderStatus.kt`: CONFIRMED に TODO コメントを追加
- [x] `OrderRepository.kt`: 未使用の `import java.util.UUID` を削除

## 実装後の振り返り

- **実装完了日**: 2026-04-11
- **計画と実績の差分**:
  - `OrderRepositoryImpl.limit()` の deprecation警告も追加で修正（レビュー指摘外）
  - `UpdatePaymentRequest` データクラス自体も削除（requestパラメータ削除に伴い不要）
- **学んだこと**:
  - non-null assertion `!!` より `?: error(...)` の方がエラーメッセージが明確で安全
  - モックAPIでも未使用パラメータは残さない（コードの意図が不明瞭になる）
