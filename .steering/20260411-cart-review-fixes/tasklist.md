# タスクリスト - カート管理PRレビュー修正

## 🚨 タスク完全完了の原則

**このファイルの全タスクが完了するまで作業を継続すること**

---

## フェーズ1: 指摘1 - added_at カラム追加

- [x] `V9__add_cart_items_added_at.sql` を作成（`added_at TIMESTAMPTZ NOT NULL DEFAULT now()` 追加）
- [x] `CartItemsTable.kt` に `addedAt` カラム追加
- [x] `CartRepositoryImpl.kt` の `save()` で `addedAt = now()` をセット
- [x] `CartRepositoryImpl.kt` の `findByCustomerId()` で `orderBy(CartItemsTable.addedAt, SortOrder.ASC)` 追加

## フェーズ2: 指摘2 - TOCTOU 競合修正

- [x] `CartRepositoryImpl.kt` の `save()` で exist check + insert を `INSERT ... ON CONFLICT DO NOTHING` 相当に変更
  - Exposed の `insertIgnore` または upsert を使用

## フェーズ3: 指摘3 - cartId nullable 解消

- [x] `CartResult.kt` から `cartId: UUID?` フィールドを削除
- [x] `GetCartUseCase.kt` の CartResult 生成部分を更新（cartId を渡さない）
- [x] `GetCartUseCaseTest.kt` のアサーションを更新（cartId 参照なし、変更不要）

## フェーズ4: 指摘4 - 削除済み商品の自動削除（代替案B）

- [x] `GetCartUseCase.kt` の `@Transactional(readOnly = true)` を `@Transactional` に変更
- [x] `GetCartUseCase.kt` で削除済み商品がある場合、更新後のカートを `cartRepository.save()` で保存
- [x] `GetCartUseCaseTest.kt` に「削除済み商品がDBから除去されること」のテストを追加

## フェーズ5: 品質チェック

- [x] テストが通ることを確認（`./gradlew test`）

---

## 実装後の振り返り

### 実装完了日
2026-04-11

### 計画と実績の差分
- `CartItem` に `addedAt` を追加したため、`Cart.addItem()` や全テストのコンストラクタ呼び出しも修正が必要だった
- `insertIgnore` は Exposed 0.56.0 で利用可能（追加依存なし）

### 学んだこと
- `sed` による一括置換は括弧ネストに注意が必要（`listOf(CartItem(...))` のような構造で誤置換発生）
- ドメインモデルの変更（`CartItem` への `addedAt` 追加）はテスト側への波及が広い
