# タスクリスト

## フェーズ1: GetCategoriesUseCase の作成
- [x] CategoryResult データクラスを作成
- [x] GetCategoriesUseCase を実装
- [x] ProductController を GetCategoriesUseCase を使うよう修正

## フェーズ2: orders.js のアニメーション index 修正
- [x] renderOrderCards にオフセット引数を追加
- [x] 追加ロード時にオフセットを渡すよう修正

## フェーズ3: 確認
- [x] 修正3（ログアウト後遷移）が既存コードで実装済みであることを確認（app.js:126 に `window.location.hash = '#/'` が既に存在）

## 実装後の振り返り

- 実装完了日: 2026-04-12
- 計画と実績の差分:
  - 修正3（ログアウト後遷移）は app.js:126 に既に `window.location.hash = '#/'` が存在しており、実装不要だった。レビュー時にdiffの文脈行が3行で打ち切られていたため、誤検知した。
- 修正内容:
  1. `GetCategoriesUseCase` + `CategoryResult` を新規作成し、`ProductController` のアーキテクチャ違反を解消
  2. `renderOrderCards(orders, offset = 0)` としてオフセット引数を追加し、追加ロード時のアニメーション index がリセットされる問題を修正
