# 設計書

## 変更方針

コードとドキュメントの2領域を修正する。新規ファイルの追加は不要。

## 変更対象ファイル

### ドメイン層

**`OrderStatus.kt`**
- `PENDING` と TODO コメントを削除
- 遷移順: `CONFIRMED → SHIPPING → DELIVERED / CANCELLED`

**`Order.kt`**
- `Order.create()` の初期ステータスを `PENDING` → `CONFIRMED` に変更
- KDoc コメント「PENDING または CONFIRMED」→「CONFIRMED」に修正

### テスト層（`PENDING` 参照を `CONFIRMED` に置き換え）

| ファイル | 変更箇所 |
|---------|---------|
| `OrderTest.kt` | デフォルト引数・when ブランチ・assertEquals・テスト名 |
| `CancelOrderUseCaseTest.kt` | テスト名・`createOrder()` 引数 |
| `UpdateShipmentUseCaseTest.kt` | テストデータの初期ステータス |
| `UpdatePaymentUseCaseTest.kt` | テストデータの初期ステータス |

### ドキュメント層

**`docs/functional-design.md`**

| 修正箇所 | 内容 |
|---------|------|
| L76 付近 | `Customer.name: String` → `CustomerName` 値オブジェクト |
| L178-180 付近 | `CartItem` に `addedAt: Instant` フィールド追記 |
| L207-213 付近 | OrderStatus から `PENDING` 削除 |
| L237-241 付近 | ビジネスルール記述から `PENDING` の言及を削除 |
| L419 付近 | Product API テーブルに `/management` エンドポイント追記 |

## 実装の順序

1. `OrderStatus.kt` の修正（PENDING 削除）
2. `Order.kt` の修正（初期ステータス変更・KDoc 修正）
3. テストファイル4件の修正
4. `docs/functional-design.md` の修正
5. テスト実行で確認
