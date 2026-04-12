# 要求内容

## 概要
レビューで指摘された3つの問題を修正する。

## 指摘事項

### 1. アーキテクチャ違反（ProductController）
`ProductController` が `CategoryRepository` を直接インジェクトしており、ユースケース層をバイパスしている。
他のすべての操作はユースケース経由のため、`GetCategoriesUseCase` を新規作成して整合させる。

### 2. ページネーションのアニメーション index リセット（orders.js）
`renderOrderCards` 関数の `--index` CSS変数が追加ロード時に常に `0` から始まるため、
カード出現アニメーションが重なる。オフセットを受け取るよう修正する。

### 3. ログアウト後のページ遷移なし（app.js）
レビュー時点では `window.location.hash = '#/'` が欠けていると判断したが、
実際のコードでは既に実装済み。対応不要。
