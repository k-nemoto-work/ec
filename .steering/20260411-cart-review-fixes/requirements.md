# 要求内容 - カート管理PRレビュー修正

## 背景

k-nemoto-work/ec#2 のPRレビューで指摘された4点のうち、以下3点を修正し、指摘4については代替案Bを採用する。

## 修正内容

### 指摘1: CartItem に順序保証がない
`cart_items` テーブルに `added_at` カラムがなく、取得時に順序が不定。
- `V9__add_cart_items_added_at.sql` でカラム追加
- `CartItemsTable.kt` にカラム追加
- `CartRepositoryImpl.kt` の insert 時に `addedAt` をセット、select 時に `orderBy(addedAt, ASC)`

### 指摘2: カート新規作成の競合状態（TOCTOU）
同一顧客が同時リクエストした場合、`UNIQUE INDEX idx_carts_customer_id` 違反が発生しうる。
- `save()` の exist check + insert を `INSERT ... ON CONFLICT DO NOTHING` に置き換え

### 指摘3: `CartResult.cartId` が nullable
APIクライアントが null を特別処理する必要がある。
- `cartId` フィールドを `CartResult` から削除（フロントエンドのカート操作に不要）
- 関連するテストも更新

### 指摘4: 削除済み商品のサイレントスキップ → 代替案B採用
`GetCartUseCase` で商品が見つからなかった場合、DBからも削除する。
- `GetCartUseCase` の `@Transactional(readOnly = true)` → `@Transactional` に変更
- 商品が見つからなかったカートアイテムを `cartRepository.save()` で削除
