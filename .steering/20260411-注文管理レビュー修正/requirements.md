# 要求内容

## 概要

PR #3「feat(order): 注文管理機能を実装」のコードレビューで発見された問題点をすべて修正する。

## 修正対象

### Bug修正

1. **`findByCustomerId` の non-null assertion が危険**
   - `paymentsByOrderId[orderId]!!` と `shipmentsByOrderId[orderId]!!` をより安全な方法に変更

2. **`UpdatePaymentRequest` が受け取られているが未使用**
   - `OrderController.updatePayment` で `@RequestBody request: UpdatePaymentRequest` を受け取るが `request` を全く使っていない
   - モック実装なのでリクエストボディ自体不要なため削除

3. **無効な enum 文字列でのクラッシュがハンドルされていない**
   - `PaymentMethod.valueOf()` と `ShipmentStatus.valueOf()` で無効な文字列を渡すと `IllegalArgumentException` が発生
   - `GlobalExceptionHandler` に `IllegalArgumentException` のハンドラーを追加

### 設計上の問題

4. **PRの説明と実装の乖離**
   - PRの説明には `DELETE /orders/{orderId}` と記載されているが実装は `PATCH /orders/{orderId}/cancel`
   - PRの説明に記載されている `PUT` も実装は `PATCH`
   - PRの説明をコードに合わせて修正（コードが正しい実装）

5. **`OrderStatus.CONFIRMED` への遷移ロジックがない**
   - PENDING → CONFIRMED への遷移を行うユースケースが存在しない
   - 現状ではCONFIRMEDが使われないため、TODO コメントを追加して意図を明示

### Minor

6. **未使用インポート**
   - `OrderRepository.kt` の `import java.util.UUID` が未使用なので削除
