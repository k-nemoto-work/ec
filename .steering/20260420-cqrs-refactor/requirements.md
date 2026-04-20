# 要求内容: CQRS-Light リファクタリング

## 背景

一覧表示などのRead操作において、ドメインリポジトリを通じた複数クエリ発行がパフォーマンス上の問題となっている。

## 要求

CQRSの概念を採用し、Read操作にクエリサービスを導入してパフォーマンスを改善する。

## 対象UseCase

- `ListProductsUseCase`: 2クエリ（list + count）→ 1クエリ（Window関数）
- `GetOrdersUseCase`: 実質5クエリ → 1クエリ（JOIN + GROUP BY）
- `GetFavoriteUseCase`: 3クエリ → 1クエリ（3テーブルJOIN）

## 制約

- ドメイン層の外部依存ゼロルールを維持
- 書き込みフロー（Write UseCase）は変更しない
- HTTP層は変更しない
- 新規DTOは作成しない（既存DTOを再利用）
