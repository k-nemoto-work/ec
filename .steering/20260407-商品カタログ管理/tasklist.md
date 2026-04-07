# タスクリスト: 商品カタログ管理

## フェーズ1: Domain層

- [x] ProductId.kt を作成
- [x] CategoryId.kt を作成
- [x] Money.kt を作成（バリデーション: 1円以上）
- [x] ProductStatus.kt を作成
- [x] Category.kt を作成
- [x] Product.kt を作成（集約ルート、ビジネスルール実装）
- [x] ProductRepository.kt インターフェースを作成
- [x] CategoryRepository.kt インターフェースを作成

## フェーズ2: Infrastructure層（DB）

- [x] V4__create_categories.sql を作成
- [x] V5__create_products.sql を作成
- [x] CategoriesTable.kt を作成
- [x] ProductsTable.kt を作成
- [x] CategoryRepositoryImpl.kt を作成
- [x] ProductRepositoryImpl.kt を作成

## フェーズ3: UseCase層

- [x] ListProductsQuery.kt / ProductSummary.kt / ProductListResult.kt を作成
- [x] ListProductsUseCase.kt を作成
- [x] ProductResult.kt を作成
- [x] GetProductUseCase.kt を作成
- [x] RegisterProductCommand.kt を作成
- [x] RegisterProductUseCase.kt を作成
- [x] UpdateProductCommand.kt を作成
- [x] UpdateProductUseCase.kt を作成
- [x] UpdateProductStatusCommand.kt を作成
- [x] UpdateProductStatusUseCase.kt を作成

## フェーズ4: HTTP層

- [x] ProductController.kt を作成

## フェーズ5: テスト

- [x] MoneyTest.kt を作成（値オブジェクトのバリデーションテスト）
- [x] ProductTest.kt を作成（ビジネスルールのユニットテスト）
- [x] ListProductsUseCaseTest.kt を作成
- [x] GetProductUseCaseTest.kt を作成
- [x] RegisterProductUseCaseTest.kt を作成
- [x] UpdateProductUseCaseTest.kt を作成
- [x] UpdateProductStatusUseCaseTest.kt を作成

## 実装後の振り返り

### 実装完了日
2026-04-07

### 計画と実績の差分

**計画通りに実装できた点:**
- 全フェーズ（Domain → Infrastructure → UseCase → HTTP → テスト）のタスクを完全完了
- クリーンアーキテクチャの依存ルールを遵守
- 既存の顧客管理パターンを踏襲した実装

**計画から変更した点:**
- `Product.update()` に `validateName()` / `validateDescription()` のバリデーション呼び出しを追加（implementation-validatorの指摘を受けて）
- `ResourceNotFoundException` の呼び出しを2引数コンストラクタに修正（コンパイルエラー対応）
- V5マイグレーションに `DROP TABLE IF EXISTS products/orders` を追加（V1の暫定スキーマとの競合対応）

### 学んだこと

1. **既存マイグレーションとの競合確認**: V1で初期スキーマとして暫定テーブルが作成されている場合、新しいマイグレーションでDROPして再作成する必要がある
2. **IDEの誤検知**: 同一パッケージ内の参照でIDEが "Unresolved reference" を表示することがあるが、Gradleビルドでは問題ない（言語サーバーのキャッシュ問題）
3. **非推奨APIの対応**: Exposed の `limit(Int, Long)` は非推奨なので `limit(Int).offset(Long)` チェーンで記述する

### 次回への改善提案

1. `ProductController` にBean Validation (`@Valid`) を追加してHTTP層での早期バリデーションを実装する
2. HTTP層の統合テスト（MockMvc）を追加する
3. `Product.changeStatus()` は将来的に手動変更用とシステム自動遷移用にメソッドを分離することを検討する（注文・配送コンテキスト実装時）
4. `CategoryId.generate()` は現在未使用 — カテゴリ管理機能実装時まで保留
