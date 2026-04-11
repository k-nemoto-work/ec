# 要求内容

## 概要

`OrderStatus.PENDING` を削除し、注文作成時から `CONFIRMED`（確定）ステータスで開始するよう修正する。
あわせて、実装と `docs/functional-design.md` の乖離箇所を修正する。

## 背景

現在 `OrderStatus` に `PENDING` と `CONFIRMED` の両方が定義されているが、
`PENDING → CONFIRMED` への遷移ロジックが未実装（TODOコメントあり）。
`PlaceOrderUseCase` は常に `PENDING` で注文を作成しており、`CONFIRMED` には実運用上到達できない。

「売り手確認フロー」はこのプロジェクトのスコープ外であるため、
`PENDING` を削除して注文作成時から `CONFIRMED` に統一する。

また、以下の実装とドキュメントの細部乖離も同時に修正する：
- `CartItem.addedAt` フィールドがドキュメントに未記載
- `Customer.name` の型が `String` → `CustomerName`（値オブジェクト）になっているが未反映
- `GET /api/v1/products/{productId}/management` エンドポイントが API 設計テーブルに未記載

## 実装対象

### 1. OrderStatus.PENDING 削除
- enum から `PENDING` を削除
- `Order.create()` の初期ステータスを `CONFIRMED` に変更
- 関連テストを更新

### 2. functional-design.md の乖離修正
- OrderStatus 定義から `PENDING` を削除
- `CartItem` に `addedAt: Instant` を追記
- `Customer.name` の型を `CustomerName` に修正
- API 設計テーブルに `/management` エンドポイントを追記

## 受け入れ条件

### OrderStatus.PENDING 削除
- [ ] `OrderStatus.PENDING` が enum に存在しない
- [ ] 注文作成時のステータスが `CONFIRMED`
- [ ] 全テストがグリーン

### functional-design.md 修正
- [ ] OrderStatus から `PENDING` が削除されている
- [ ] `CartItem` に `addedAt` フィールドが記載されている
- [ ] `Customer.name` の型が `CustomerName` になっている
- [ ] `/api/v1/products/{productId}/management` が API テーブルに記載されている

## スコープ外

- 新しい UseCase の追加
- DB マイグレーションの変更
- HTTP 層のレスポンス形式の変更
