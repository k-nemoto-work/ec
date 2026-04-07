# 設計: 商品カタログ管理

## 実装方針

既存の顧客管理機能のパターンに従い、クリーンアーキテクチャ4層構成で実装する。

## ファイル構成

### Domain層
```
src/main/kotlin/com/example/ec/domain/product/
├── ProductId.kt         # 値オブジェクト
├── ProductName.kt       # 値オブジェクト
├── CategoryId.kt        # 値オブジェクト
├── Money.kt             # 値オブジェクト
├── ProductStatus.kt     # 列挙型
├── Product.kt           # 集約ルート
├── Category.kt          # エンティティ
├── ProductRepository.kt # リポジトリインターフェース
└── CategoryRepository.kt# リポジトリインターフェース
```

### UseCase層
```
src/main/kotlin/com/example/ec/usecase/product/
├── list/
│   ├── ListProductsQuery.kt
│   ├── ProductSummary.kt
│   ├── ProductListResult.kt
│   └── ListProductsUseCase.kt
├── get/
│   ├── ProductResult.kt
│   ├── GetProductUseCase.kt
│   └── GetProductForManagementUseCase.kt
├── register/
│   ├── RegisterProductCommand.kt
│   └── RegisterProductUseCase.kt
├── update/
│   ├── UpdateProductCommand.kt
│   └── UpdateProductUseCase.kt
└── update_status/
    ├── UpdateProductStatusCommand.kt
    └── UpdateProductStatusUseCase.kt
```

### Infrastructure層
```
src/main/kotlin/com/example/ec/infrastructure/
├── table/
│   ├── CategoriesTable.kt
│   └── ProductsTable.kt
└── repository/
    ├── CategoryRepositoryImpl.kt
    └── ProductRepositoryImpl.kt

src/main/resources/db/migration/
├── V4__create_categories.sql
└── V5__create_products.sql
```

### HTTP層
```
src/main/kotlin/com/example/ec/http/controller/
└── ProductController.kt
```

## ビジネスルール

1. 商品名: 1〜100文字（空白不可）
2. 価格: 1円以上（Money値オブジェクトで検証）
3. 説明: 2000文字以内（空文字可）
4. ステータス遷移: 特定の遷移のみ許可
   - PRIVATE → ON_SALE（公開）
   - ON_SALE → PRIVATE（非公開）
   - ON_SALE → RESERVED（注文確定時。UseCase側で制御）
   - RESERVED → SOLD（配送完了時。UseCase側で制御）
   - RESERVED → ON_SALE（注文キャンセルによる復元。UseCase側で制御）
5. 購入者向け一覧・詳細はON_SALEのみ返す

## ステータス遷移の設計

Product.changeStatus()メソッドで遷移の妥当性を検証する。
不正な遷移はBusinessRuleViolationExceptionをスローする。

許可する手動変更（/api/v1/products/{id}/status エンドポイント）:
- PRIVATE → ON_SALE
- ON_SALE → PRIVATE

内部制御のみ（他コンテキストから呼ばれる）:
- ON_SALE → RESERVED（注文確定）
- RESERVED → SOLD（配送完了）
- RESERVED → ON_SALE（注文キャンセルによる復元）

## テスト方針

- Domain層: Money, Product の値オブジェクト・ビジネスルールのユニットテスト
- UseCase層: MockKを使ったユニットテスト（全ユースケース）
