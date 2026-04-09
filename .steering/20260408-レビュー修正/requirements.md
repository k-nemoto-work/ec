# 要求: お気に入り管理機能のレビュー指摘修正

## 背景

PR #1 のコードレビューで以下の指摘が挙がった。全て修正する。

## 修正項目

1. **FavoriteRepository.save/update の設計問題**
   - `save` と `update` が分離されており、ユースケース層が `isNew` フラグで永続化の詳細を判断している
   - `save` に統合し、リポジトリ内でupsert/差分更新を行う

2. **FavoriteRepositoryImpl の delete-all + re-insert**
   - `update()` が全アイテム削除 → 再挿入しており、並行リクエスト時にデータ消失リスクがある
   - 差分更新（追加分だけINSERT、削除分だけDELETE）に変更

3. **アイテムの取得順序が非決定的**
   - `findByCustomerId` のアイテムクエリに ORDER BY がない
   - `added_at ASC` で順序付け

4. **GetFavoriteUseCaseTest に削除済み商品のテストがない**
   - 商品がカタログから削除された場合に無言でスキップされる挙動が未テスト
   - テストを追加し、意図を明示

5. **SQL マイグレーションに FK なしの説明コメントがない**
   - `favorite_items.product_id` に外部キーがない理由が不明
   - コメントで意図を明示

6. **FavoriteController の @Valid 欠如**
   - `AddToFavoriteRequest` に `@Valid` がない
   - `@field:NotNull` + `@Valid` を追加
