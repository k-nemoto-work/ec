# リポジトリ構造定義書 (Repository Structure Document)

## プロジェクト全体構造

```
ec/                                    # プロジェクトルート
├── src/
│   ├── main/
│   │   ├── kotlin/com/example/ec/     # アプリケーション本体
│   │   └── resources/                 # 設定・マイグレーション
│   └── test/
│       └── kotlin/com/example/ec/     # テストコード
├── docs/                              # プロジェクトドキュメント
├── .steering/                         # 作業単位ドキュメント
├── .claude/                           # Claude Code設定
├── build.gradle.kts                   # Gradleビルド設定（Kotlin DSL）
├── settings.gradle.kts
├── .gitignore
└── README.md
```

---

## ソースコード構造

```
src/main/kotlin/com/example/ec/
├── http/                              # HTTP層
│   ├── controller/                    #   REST Controller
│   │   ├── AuthController.kt          #     認証エンドポイント
│   │   ├── CustomerController.kt
│   │   ├── ProductController.kt
│   │   ├── CartController.kt
│   │   ├── FavoriteController.kt
│   │   └── OrderController.kt
│   ├── presenter/                     #   UseCaseレスポンス → HTTPレスポンス整形（ユースケース単位）
│   │   ├── customer/
│   │   │   ├── GetCustomerProfilePresenter.kt
│   │   │   └── LoginPresenter.kt
│   │   ├── product/
│   │   │   ├── ListProductsPresenter.kt
│   │   │   └── GetProductPresenter.kt
│   │   ├── cart/
│   │   │   └── GetCartPresenter.kt
│   │   ├── favorite/
│   │   │   └── GetFavoritesPresenter.kt
│   │   └── order/
│   │       ├── PlaceOrderPresenter.kt
│   │       ├── GetOrderPresenter.kt
│   │       └── ListOrdersPresenter.kt
│   ├── filter/                        #   Spring Security フィルター
│   │   └── JwtAuthenticationFilter.kt
│   └── advice/                        #   例外ハンドラー
│       ├── GlobalExceptionHandler.kt  #     @ExceptionHandler でドメイン例外をHTTPに変換
│       └── ErrorResponse.kt           #     共通エラーレスポンス形式
│
├── usecase/                           # UseCase層（ユースケース単位でディレクトリ化）
│   ├── customer/
│   │   ├── register/
│   │   │   ├── RegisterCustomerUseCase.kt
│   │   │   └── RegisterCustomerCommand.kt
│   │   ├── login/
│   │   │   ├── LoginUseCase.kt
│   │   │   ├── LoginCommand.kt
│   │   │   └── LoginResult.kt
│   │   ├── get_profile/
│   │   │   ├── GetCustomerProfileUseCase.kt
│   │   │   └── CustomerProfileResult.kt
│   │   └── update_address/
│   │       ├── UpdateAddressUseCase.kt
│   │       └── UpdateAddressCommand.kt
│   ├── product/
│   │   ├── list/
│   │   │   ├── ListProductsUseCase.kt
│   │   │   ├── ListProductsQuery.kt
│   │   │   └── ProductSummaryResult.kt
│   │   ├── get/
│   │   │   ├── GetProductUseCase.kt
│   │   │   └── ProductResult.kt
│   │   ├── register/
│   │   │   ├── RegisterProductUseCase.kt
│   │   │   └── RegisterProductCommand.kt
│   │   ├── update/
│   │   │   ├── UpdateProductUseCase.kt
│   │   │   └── UpdateProductCommand.kt
│   │   └── change_status/
│   │       ├── ChangeProductStatusUseCase.kt
│   │       └── ChangeProductStatusCommand.kt
│   ├── cart/
│   │   ├── get/
│   │   │   ├── GetCartUseCase.kt
│   │   │   └── CartResult.kt
│   │   ├── add/
│   │   │   ├── AddToCartUseCase.kt
│   │   │   └── AddToCartCommand.kt
│   │   └── remove/
│   │       ├── RemoveFromCartUseCase.kt
│   │       └── RemoveFromCartCommand.kt
│   ├── favorite/
│   │   ├── get/
│   │   │   ├── GetFavoritesUseCase.kt
│   │   │   └── FavoriteResult.kt
│   │   ├── add/
│   │   │   ├── AddToFavoriteUseCase.kt
│   │   │   └── AddToFavoriteCommand.kt
│   │   └── remove/
│   │       ├── RemoveFromFavoriteUseCase.kt
│   │       └── RemoveFromFavoriteCommand.kt
│   └── order/
│       ├── place/
│       │   ├── PlaceOrderUseCase.kt
│       │   ├── PlaceOrderCommand.kt
│       │   └── PlaceOrderResult.kt
│       ├── get/
│       │   ├── GetOrderUseCase.kt
│       │   └── OrderResult.kt
│       ├── list/
│       │   ├── ListOrdersUseCase.kt
│       │   └── OrderSummaryResult.kt
│       ├── cancel/
│       │   ├── CancelOrderUseCase.kt
│       │   └── CancelOrderCommand.kt
│       ├── update_payment/
│       │   ├── UpdatePaymentStatusUseCase.kt
│       │   └── UpdatePaymentCommand.kt
│       └── update_shipment/
│           ├── UpdateShipmentStatusUseCase.kt
│           └── UpdateShipmentCommand.kt
│
├── domain/                            # Domain層
│   ├── customer/                      #   顧客コンテキスト
│   │   ├── Customer.kt                #     集約ルート
│   │   ├── CustomerId.kt              #     値オブジェクト
│   │   ├── Email.kt                   #     値オブジェクト
│   │   ├── Address.kt                 #     値オブジェクト
│   │   ├── CustomerStatus.kt          #     列挙型
│   │   ├── Favorite.kt                #     集約ルート
│   │   ├── FavoriteItem.kt
│   │   ├── FavoriteId.kt
│   │   ├── CustomerRepository.kt      #     Repositoryインターフェース
│   │   └── FavoriteRepository.kt
│   ├── product/                       #   カタログコンテキスト
│   │   ├── Product.kt                 #     集約ルート
│   │   ├── ProductId.kt
│   │   ├── Money.kt                   #     値オブジェクト
│   │   ├── ProductStatus.kt
│   │   ├── Category.kt
│   │   ├── CategoryId.kt
│   │   ├── ProductRepository.kt
│   │   └── CategoryRepository.kt
│   ├── order/                         #   注文コンテキスト
│   │   ├── Cart.kt                    #     集約ルート
│   │   ├── CartId.kt
│   │   ├── CartItem.kt
│   │   ├── Order.kt                   #     集約ルート
│   │   ├── OrderId.kt
│   │   ├── OrderItem.kt
│   │   ├── OrderStatus.kt
│   │   ├── Payment.kt
│   │   ├── PaymentStatus.kt
│   │   ├── PaymentMethod.kt
│   │   ├── Shipment.kt
│   │   ├── ShipmentStatus.kt
│   │   ├── CartRepository.kt
│   │   └── OrderRepository.kt
│   └── exception/                     #   ドメイン例外
│       ├── DomainValidationException.kt
│       ├── BusinessRuleViolationException.kt
│       ├── ResourceNotFoundException.kt
│       └── UnauthorizedAccessException.kt
│
└── infrastructure/                    # Infrastructure層
    ├── repository/                    #   Repositoryインターフェースの実装
    │   ├── CustomerRepositoryImpl.kt
    │   ├── FavoriteRepositoryImpl.kt
    │   ├── ProductRepositoryImpl.kt
    │   ├── CategoryRepositoryImpl.kt
    │   ├── CartRepositoryImpl.kt
    │   └── OrderRepositoryImpl.kt
    ├── table/                         #   Exposed テーブルDSL定義
    │   ├── CustomersTable.kt
    │   ├── AddressesTable.kt
    │   ├── FavoritesTable.kt
    │   ├── FavoriteItemsTable.kt
    │   ├── ProductsTable.kt
    │   ├── CategoriesTable.kt
    │   ├── CartsTable.kt
    │   ├── CartItemsTable.kt
    │   ├── OrdersTable.kt
    │   ├── OrderItemsTable.kt
    │   ├── PaymentsTable.kt
    │   └── ShipmentsTable.kt
    └── config/                        #   Spring設定
        ├── SecurityConfig.kt          #     Spring Security設定
        ├── JwtConfig.kt               #     JWT設定（秘密鍵・有効期限）
        ├── DatabaseConfig.kt          #     Exposed / DataSource設定
        └── JwtTokenProvider.kt        #     JWT生成・検証

src/main/resources/
├── db/migration/                      # Flyway SQLマイグレーション
│   ├── V1__create_categories.sql
│   ├── V2__create_customers.sql
│   ├── V3__create_addresses.sql
│   ├── V4__create_products.sql
│   ├── V5__create_carts.sql
│   ├── V6__create_cart_items.sql
│   ├── V7__create_favorites.sql
│   ├── V8__create_favorite_items.sql
│   ├── V9__create_orders.sql
│   ├── V10__create_order_items.sql
│   ├── V11__create_payments.sql
│   └── V12__create_shipments.sql
└── application.yml                    # アプリケーション設定（DB接続・JWT等）
```

---

## テストコード構造

```
src/test/kotlin/com/example/ec/
├── domain/                            # Domain層ユニットテスト
│   ├── customer/
│   │   ├── CustomerTest.kt            #   Customer集約のビジネスルールテスト
│   │   ├── EmailTest.kt               #   Email値オブジェクトのバリデーションテスト
│   │   └── FavoriteTest.kt
│   ├── product/
│   │   ├── ProductTest.kt
│   │   └── MoneyTest.kt
│   └── order/
│       ├── CartTest.kt
│       └── OrderTest.kt
│
├── usecase/                           # UseCase層ユニットテスト（MockKでRepository mock）
│   ├── customer/
│   │   ├── register/
│   │   │   └── RegisterCustomerUseCaseTest.kt
│   │   ├── login/
│   │   │   └── LoginUseCaseTest.kt
│   │   ├── get_profile/
│   │   │   └── GetCustomerProfileUseCaseTest.kt
│   │   └── update_address/
│   │       └── UpdateAddressUseCaseTest.kt
│   ├── product/
│   │   ├── register/
│   │   │   └── RegisterProductUseCaseTest.kt
│   │   ├── update/
│   │   │   └── UpdateProductUseCaseTest.kt
│   │   └── change_status/
│   │       └── ChangeProductStatusUseCaseTest.kt
│   ├── cart/
│   │   ├── add/
│   │   │   └── AddToCartUseCaseTest.kt
│   │   ├── get/
│   │   │   └── GetCartUseCaseTest.kt
│   │   └── remove/
│   │       └── RemoveFromCartUseCaseTest.kt
│   ├── favorite/
│   │   ├── add/
│   │   │   └── AddToFavoriteUseCaseTest.kt
│   │   ├── get/
│   │   │   └── GetFavoritesUseCaseTest.kt
│   │   └── remove/
│   │       └── RemoveFromFavoriteUseCaseTest.kt
│   └── order/
│       ├── place/
│       │   └── PlaceOrderUseCaseTest.kt
│       ├── cancel/
│       │   └── CancelOrderUseCaseTest.kt
│       ├── update_payment/
│       │   └── UpdatePaymentStatusUseCaseTest.kt
│       └── update_shipment/
│           └── UpdateShipmentStatusUseCaseTest.kt
│
├── infrastructure/                    # Infrastructure層統合テスト（TestContainers）
│   ├── repository/
│   │   ├── CustomerRepositoryImplTest.kt
│   │   ├── ProductRepositoryImplTest.kt
│   │   ├── CartRepositoryImplTest.kt
│   │   └── OrderRepositoryImplTest.kt
│   └── support/
│       └── AbstractRepositoryTest.kt  #   TestContainers共通ベースクラス
│
└── http/                              # HTTP層統合テスト（MockMvc）
    ├── controller/
    │   ├── AuthControllerTest.kt
    │   ├── CartControllerTest.kt
    │   └── OrderControllerTest.kt
    └── support/
        └── AbstractControllerTest.kt  #   MockMvc共通ベースクラス
```

---

## ディレクトリ詳細

### http/controller/
**役割**: HTTPリクエストを受け付け、UseCaseを呼び出す。  
**依存可能**: `usecase/`  
**禁止**: ドメインロジックの実装、`domain/` / `infrastructure/` への直接依存

### http/presenter/
**役割**: UseCaseの出力（Result）をHTTPレスポンス用のJSON構造に整形する。ユースケース単位でクラスを作成する。  
**依存可能**: `usecase/` 配下のResultクラス  
**禁止**: UseCaseの呼び出し、ドメインオブジェクトへの直接依存

### usecase/{context}/{usecase}/
**役割**: 1ディレクトリ = 1ユースケース。UseCase本体・Command（入力）・Result（出力）を同じディレクトリに配置する。  
HTTP層とUseCase層の境界を明確にする。  
**注意**: PresenterはResultを受け取り整形するが、Resultに表示ロジックを入れない

### domain/
**役割**: ビジネスルールを完全に封じ込める。  
**禁止**: Springアノテーション・Exposed・HTTPへの依存（ゼロ依存原則）

### infrastructure/table/
**役割**: ExposedのDSLでDBテーブル構造を定義。  
**禁止**: ビジネスロジックの記述

### db/migration/
**規則**: ファイルを一度コミットしたら変更しない。修正は新しいバージョンのSQLで対応する。

---

## ファイル配置規則

### ソースファイル命名規則

| 種別 | 命名規則 | 例 |
|------|---------|-----|
| 集約ルート | PascalCase | `Customer.kt`, `Order.kt` |
| 値オブジェクト | PascalCase | `Email.kt`, `Money.kt` |
| 列挙型 | PascalCase + Status/Method | `OrderStatus.kt`, `PaymentMethod.kt` |
| Repositoryインターフェース | PascalCase + Repository | `CustomerRepository.kt` |
| Repository実装 | PascalCase + RepositoryImpl | `CustomerRepositoryImpl.kt` |
| UseCase | PascalCase + UseCase（動詞始まり）| `PlaceOrderUseCase.kt` |
| Command / Query | PascalCase + Command / Query | `PlaceOrderCommand.kt` |
| Result | PascalCase + Result | `PlaceOrderResult.kt`, `CartResult.kt` |
| Controller | PascalCase + Controller（リソース単位）| `OrderController.kt` |
| Presenter | PascalCase + Presenter（ユースケース単位）| `PlaceOrderPresenter.kt`, `GetCartPresenter.kt` |
| テーブルDSL | PascalCase + Table（複数形） | `OrdersTable.kt` |

### テストファイル命名規則

| 種別 | 命名規則 | 例 |
|------|---------|-----|
| ドメインテスト | `{対象}Test.kt` | `CartTest.kt` |
| UseCaseテスト | `{対象}Test.kt` | `PlaceOrderUseCaseTest.kt` |
| Repositoryテスト | `{対象}Test.kt` | `CartRepositoryImplTest.kt` |
| Controllerテスト | `{対象}Test.kt` | `OrderControllerTest.kt` |

### Flywayマイグレーション命名規則

`V{連番}__{説明}.sql`（アンダースコア2つ）

```
V1__create_categories.sql
V2__create_customers.sql
```

---

## 依存関係のルール

```
http/ → usecase/ → domain/
                      ↑
         infrastructure/ （DIによる依存逆転）
```

| 層 | 依存可能 | 依存禁止 |
|---|---|---|
| http/ | usecase/ | domain/, infrastructure/ |
| usecase/ | domain/（インターフェースのみ） | infrastructure/, http/ |
| domain/ | なし（ゼロ依存） | 全ての外部ライブラリ |
| infrastructure/ | domain/ | usecase/, http/ |

---

## ドキュメント構成

```
docs/
├── ideas/                             # 壁打ち・ブレインストーミング（自由形式）
│   ├── background.md
│   └── domain-model-idea.md
├── product-requirements.md            # プロダクト要求定義書
├── functional-design.md               # 機能設計書
├── architecture.md                    # アーキテクチャ設計書
├── repository-structure.md            # 本ドキュメント
├── development-guidelines.md          # 開発ガイドライン
├── glossary.md                        # ユビキタス言語定義
└── domain-model.puml                  # ドメインモデル図（PlantUML）
```

---

## 除外設定（.gitignore）

```
# ビルド成果物
build/
.gradle/

# 環境設定（秘密鍵等を含む可能性あり）
.env
application-local.yml

# IDE
.idea/
*.iml

# OS
.DS_Store

# ログ
*.log
```
