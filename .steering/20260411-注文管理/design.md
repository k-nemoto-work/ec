# 設計書

## アーキテクチャ概要

既存の Cart 実装と同じ DDD レイヤードアーキテクチャパターンを踏襲する。

```
HTTP層 (OrderController)
  ↓
UseCase層 (PlaceOrderUseCase, GetOrderUseCase, etc.)
  ↓
Domain層 (Order aggregate, OrderRepository interface)
  ↑
Infrastructure層 (OrderRepositoryImpl, Exposed tables, Flyway migrations)
```

## コンポーネント設計

### 1. Domain層

**責務**:
- `Order` 集約ルート（ビジネスルール全実装）
- 値オブジェクト: `OrderId`, `OrderItem`, `Payment`, `Shipment`
- 列挙型: `OrderStatus`, `PaymentMethod`, `PaymentStatus`, `ShipmentStatus`
- `OrderRepository` インターフェース

**実装の要点**:
- `Order.cancel()`: SHIPPING/DELIVERED 後はキャンセル不可 → `BusinessRuleViolationException`
- `Order.updatePayment()`: PAID に変更するだけ（シンプルモック）
- `Order.updateShipment()`: PAID でないと SHIPPED に進められない → `BusinessRuleViolationException`; DELIVERED 時は商品ステータス変更の責務は UseCase が担う
- `OrderItem` は注文時の商品名・価格スナップショットを保持

### 2. UseCase層

**責務**:
- `PlaceOrderUseCase`: カート取得 → 商品一括取得 → Order 生成 → 商品 RESERVED → カートクリア → 保存（全体 @Transactional）
- `GetOrdersUseCase`: 注文履歴一覧取得（ページネーション）
- `GetOrderUseCase`: 注文詳細取得（他人アクセスは 403）
- `CancelOrderUseCase`: キャンセル + 商品 ON_SALE 戻し
- `UpdatePaymentUseCase`: 決済ステータス更新
- `UpdateShipmentUseCase`: 配送ステータス更新 + DELIVERED 時は商品 SOLD に変更

**実装の要点**:
- `PlaceOrderUseCase` は `@Transactional` で全操作を1トランザクション
- 他人の注文アクセスは `UnauthorizedAccessException` → 403
- 既存の `BusinessRuleViolationException` / `ResourceNotFoundException` を活用

### 3. Infrastructure層

**責務**:
- `OrdersTable`, `OrderItemsTable`, `PaymentsTable`, `ShipmentsTable` (Exposed テーブル定義)
- `OrderRepositoryImpl` (Exposed DSL による CRUD)
- Flyway マイグレーション (V10〜V13)

**実装の要点**:
- `OrderRepository.save()` は upsert パターン（Order 新規 + items 差分更新）
- Payments / Shipments は order_id に紐づく 1:1 テーブル (insertOrUpdate)
- `findByCustomerId()` は orderedAt 降順、ページネーション対応

### 4. HTTP層

**責務**:
- `OrderController`: 6エンドポイントを実装
- JWT から customerId を取得
- リクエスト DTO バリデーション

## データフロー

### 注文確定フロー
```
1. POST /api/v1/orders (認証済み)
2. OrderController → PlaceOrderUseCase.execute(customerId)
3. CartRepository.findByCustomerId → Cart (空チェック)
4. ProductRepository.findAllByIds → List<Product> (ON_SALE チェック)
5. Order.create(customerId, products, shippingAddress)
6. 各 Product.reserve() → status = RESERVED
7. OrderRepository.save(order)
8. ProductRepository.saveAll(updatedProducts)
9. CartRepository.clearItems(cartId)
10. 201 Created { orderId }
```

### 配送完了フロー
```
1. PATCH /api/v1/orders/{orderId}/shipment { status: "DELIVERED" }
2. UpdateShipmentUseCase.execute(orderId, customerId, status)
3. OrderRepository.findById → Order
4. order.updateShipment(DELIVERED) (PAID チェック)
5. ProductRepository.findAllByIds(order.items) → 各商品の status を SOLD に変更
6. OrderRepository.save(updatedOrder)
7. ProductRepository.saveAll(updatedProducts)
```

## エラーハンドリング戦略

### 既存例外クラスを再利用

| 状況 | 例外クラス | HTTP |
|---|---|---|
| 注文/商品が見つからない | `ResourceNotFoundException` | 404 |
| カートが空 / ON_SALE 以外 / キャンセル不可 / 配送ステータス不正 | `BusinessRuleViolationException` | 409 |
| 他人の注文へのアクセス | `UnauthorizedAccessException` (新規作成) | 403 |

`UnauthorizedAccessException` は既存の `AuthenticationException` 体系に追加する（`GlobalExceptionHandler` で 403 マッピング）。

## テスト戦略

### Domain ユニットテスト
- `OrderTest`: cancel/updatePayment/updateShipment のビジネスルール
- 正常系・異常系（不正なステータス遷移など）

### UseCase ユニットテスト
- `PlaceOrderUseCaseTest`: 正常系・カート空・ON_SALE 以外
- `CancelOrderUseCaseTest`: 正常系・キャンセル不可ステータス
- `UpdateShipmentUseCaseTest`: DELIVERED 時の商品ステータス変更

## ディレクトリ構造

```
src/main/kotlin/com/example/ec/
├── domain/order/
│   ├── Order.kt                (新規)
│   ├── OrderId.kt              (新規)
│   ├── OrderItem.kt            (新規)
│   ├── OrderStatus.kt          (新規)
│   ├── Payment.kt              (新規)
│   ├── Shipment.kt             (新規)
│   └── OrderRepository.kt     (新規)
├── domain/exception/
│   └── UnauthorizedAccessException.kt  (新規)
├── usecase/order/
│   ├── place/
│   │   ├── PlaceOrderUseCase.kt
│   │   └── PlaceOrderResult.kt
│   ├── get/
│   │   ├── GetOrderUseCase.kt
│   │   ├── GetOrdersUseCase.kt
│   │   ├── OrderResult.kt
│   │   └── OrderSummaryResult.kt
│   ├── cancel/
│   │   └── CancelOrderUseCase.kt
│   ├── update_payment/
│   │   ├── UpdatePaymentUseCase.kt
│   │   └── UpdatePaymentCommand.kt
│   └── update_shipment/
│       ├── UpdateShipmentUseCase.kt
│       └── UpdateShipmentCommand.kt
├── infrastructure/
│   ├── table/
│   │   ├── OrdersTable.kt      (新規)
│   │   ├── OrderItemsTable.kt  (新規)
│   │   ├── PaymentsTable.kt    (新規)
│   │   └── ShipmentsTable.kt   (新規)
│   └── repository/
│       └── OrderRepositoryImpl.kt  (新規)
└── http/controller/
    └── OrderController.kt          (新規)

src/main/resources/db/migration/
├── V10__create_orders.sql
├── V11__create_order_items.sql
├── V12__create_payments.sql
└── V13__create_shipments.sql

src/test/kotlin/com/example/ec/
├── domain/order/
│   └── OrderTest.kt
└── usecase/order/
    ├── place/PlaceOrderUseCaseTest.kt
    ├── cancel/CancelOrderUseCaseTest.kt
    ├── update_payment/UpdatePaymentUseCaseTest.kt
    └── update_shipment/UpdateShipmentUseCaseTest.kt
```

## 実装の順序

1. Domain層（Order集約、値オブジェクト、Repository interface）
2. Infrastructure層（Flyway migrations、Exposed tables、Repository実装）
3. UseCase層（全6ユースケース）
4. HTTP層（OrderController）
5. テスト（Domain → UseCase順）

## セキュリティ考慮事項

- 注文取得・更新は必ず customerId を照合（他人の注文操作を拒否）
- JWT 認証済みエンドポイントのみ（SecurityConfig は `anyRequest().authenticated()` で既にカバー）

## パフォーマンス考慮事項

- 注文一覧は LIMIT/OFFSET でページネーション
- `PlaceOrderUseCase` は `findAllByIds` で商品を一括取得（N+1 回避）
