# 設計

## 修正1: GetCategoriesUseCase の新規作成

### 方針
- `src/main/kotlin/com/example/ec/usecase/product/get_categories/` ディレクトリを作成
- `CategoryResult` データクラスを定義（id: UUID, name: String）
- `GetCategoriesUseCase` を `@Service` として実装
- `ProductController` から `CategoryRepository` の直接依存を除去し、`GetCategoriesUseCase` に差し替え

### 既存パターンに合わせる点
- `@Service` + `@Transactional(readOnly = true)` アノテーション
- `execute()` メソッドで結果を返す

## 修正2: renderOrderCards のオフセット対応

### 方針
- `renderOrderCards(orders, offset = 0)` のようにオフセット引数を追加
- `--index:${offset + i}` に変更
- 初回レンダリング: `offset = 0`
- 追加ロード: `offset = allOrders.length - more.length`（concat前の件数）

## 修正3: スキップ
app.js の `window.location.hash = '#/'` は既に実装済みのため対応不要。
