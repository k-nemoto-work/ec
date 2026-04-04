# タスクリスト

## フェーズ1: 横断的な矛盾修正（高優先度）

- [x] 1-1. development-guidelines.md: Money型を`Int`→`Long`に修正
- [x] 1-2. glossary.md: PaymentStatus.REFUNDEDにスコープ外注記を追加
- [x] 1-3. architecture.md: テストパッケージパス`reuse`→`ec`に修正
- [x] 1-4. glossary.md: CartItemの「価格（スナップショット）を持つ」を修正
- [x] 1-5. repository-structure.md: src/の重複記述を修正

## フェーズ2: architecture.md の中優先度修正

- [x] 2-1. パッケージ構成にFavoriteRepository.kt, CategoryRepositoryImpl.ktを追加し、usecase/の詳細をrepository-structure.md参照に変更
- [x] 2-2. JWT有効期限の具体値を追記
- [x] 2-3. パフォーマンス測定前提条件を追記

## フェーズ3: functional-design.md の中優先度修正

- [x] 3-1. 注文確定フローのシーケンス図にカートクリア処理を追加
- [x] 3-2. カートレスポンスの合計金額計算方法を追記
- [x] 3-3. 商品登録・更新APIのリクエスト/レスポンス仕様を追加
- [x] 3-4. 注文確定のトランザクション方針を明記

## フェーズ4: development-guidelines.md の中優先度修正

- [x] 4-1. Lintツール（ktlint）のコマンドをコミット前チェックリストに追加
- [x] 4-2. UseCaseテストの「主要フロー100%」を具体的に定義

## フェーズ5: glossary.md の中優先度修正

- [x] 5-1. 注文/決済/配送ステータスの状態遷移を追加
- [x] 5-2. 「出品者」「購入者」のロール用語を追加

## フェーズ6: repository-structure.md の中優先度修正

- [x] 6-1. テストコードのusecase/セクションの省略部分を展開

## 実装後の振り返り

**実装完了日**: 2026-04-04

**修正概要**:
- 全17タスクを完了
- 5ドキュメント（functional-design.md, architecture.md, development-guidelines.md, glossary.md, repository-structure.md）を修正
- 横断的な矛盾4件（Money型、PaymentStatus、テストパッケージ、CartItem定義）を一括解消

**計画と実績の差分**: 計画通りに全タスクを完了。特に乖離なし。

**学んだこと**:
- ドキュメント間の矛盾は横断的に発生しやすく、1箇所の修正が複数ドキュメントに波及する
- 用語集（glossary.md）が「正」となるべきだが、機能設計書との間でも矛盾が発生していた
