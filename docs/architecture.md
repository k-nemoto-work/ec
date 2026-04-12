# 技術仕様書 (Architecture Design Document)

## テクノロジースタック

### 言語・ランタイム

| 技術 | バージョン | 選定理由 |
|------|-----------|----------|
| Kotlin | 1.9.x | `data class` / `sealed class` がDDDの値オブジェクト・ドメインイベント表現に最適。Javaより簡潔・安全なコードが書ける |
| JVM (Java) | 21 (LTS) | 2026年9月まで長期サポート。Virtual Threads（Project Loom）が使用可能 |

### フレームワーク・ライブラリ

| 技術 | バージョン | 用途 | 選定理由 |
|------|-----------|------|----------|
| Spring Boot | 3.x | アプリケーションフレームワーク | Kotlin/JVMのデファクトスタンダード。DI・認証・DB連携を網羅 |
| Spring Security | 6.x | 認証・認可 | Spring標準の認証ライブラリ。JWTフィルターを組み込みやすい |
| Spring Web MVC | 6.x | REST APIサーバー | アノテーションベースのController定義。MockMvcテストと相性良い |
| Exposed | 0.5x | DB操作（ORM） | JetBrains製Kotlinネイティブ DSL。型安全なSQLでJPAのインピーダンスミスマッチを回避 |
| Flyway | 10.x | DBマイグレーション | SQLファイルベースのスキーマ変更管理。Flywayのバージョン管理でロールバック可能 |
| jjwt (JJWT) | 0.12.x | JWT生成・検証 | Javaエコシステムで広く使われるJWTライブラリ |
| SpringDoc OpenAPI | 2.x | APIドキュメント自動生成 | コードからSwagger UIを生成。OpenAPI 3.0対応 |
| BCrypt | Spring Security内包 | パスワードハッシュ | Spring Security標準。ソルト付きハッシュでレインボーテーブル攻撃対策済み |

### テスト

| 技術 | バージョン | 用途 | 選定理由 |
|------|-----------|------|----------|
| JUnit 5 | 5.x | テストフレームワーク | Kotlin対応。`@Test`でバッククォート関数名による日本語テスト名が使える |
| MockK | 1.x | モックライブラリ | Kotlin向けモックライブラリ。`mockk<T>()` でKotlinの型システムに自然に統合 |
| TestContainers | 1.x | DB統合テスト | PostgreSQLコンテナを起動し実DB環境でテスト。CIでも動作 |
| Spring Boot Test | Spring Boot同梱 | 統合テスト基盤 | `@SpringBootTest` / MockMvc でHTTP層テスト |

### 開発環境・ツール

| 技術 | バージョン | 用途 | 選定理由 |
|------|-----------|------|----------|
| Gradle | 8.x (Kotlin DSL) | ビルドツール | Spring Boot標準。`build.gradle.kts` でKotlinで設定を書ける |
| Docker / devcontainer | - | 開発環境 | 環境差異をゼロにする。PostgreSQLも同一コンテナ内で起動 |
| PostgreSQL | 16 | データベース | MySQLとの差分学習。UUID型・JSONBなど高度な機能を持つ |

---

## アーキテクチャパターン

### クリーンアーキテクチャ（4層構成）

```
┌───────────────────────────────────┐
│  HTTP層                           │  ← REST Controller / JWT Filter
│  (presentation)                   │     リクエスト受付・レスポンス変換
├───────────────────────────────────┤
│  UseCase層                        │  ← Application Service
│  (application)                    │     ユースケースの実行・トランザクション管理
├───────────────────────────────────┤
│  Domain層                         │  ← Aggregate / Entity / ValueObject
│  (domain)                         │     ビジネスルール・Repositoryインターフェース
├───────────────────────────────────┤
│  Infrastructure層                 │  ← Repository実装 / DBマッパー
│  (infrastructure)                 │     Exposed DSL / Flyway
└───────────────────────────────────┘
```

**依存の方向**: HTTP → UseCase → Domain ← Infrastructure

DIによる依存性逆転（DIP）: Domain層がRepositoryインターフェースを定義し、Infrastructure層が実装する。UseCase層はインターフェースのみに依存する。

### 各層の責務と制約

#### HTTP層
- **責務**:
  - Controller: リクエストのデシリアライズ・バリデーション、JWT Cookie からの`customerId`抽出、UseCaseの呼び出し
  - Presenter: UseCaseの出力をHTTPレスポンス用に整形する
- **許可**: UseCaseの呼び出し、Presenterによるレスポンス整形
- **禁止**: ドメインロジックの実装、Repositoryへの直接アクセス

#### UseCase層
- **責務**: 1ユースケース = 1クラス、トランザクション境界の管理（`@Transactional`）、Domainオブジェクトの取得・操作・保存の調整、UseCaseのI/O定義（Command / Response）
- **許可**: Repositoryインターフェース（Domain定義）の呼び出し、Domainオブジェクトの操作
- **禁止**: HTTP・DBの詳細への依存

#### Domain層
- **責務**: 集約・値オブジェクト・ドメインサービスの実装、Repositoryインターフェースの定義、ビジネスルールの完全な封じ込め
- **許可**: 純粋なKotlinコードのみ
- **禁止**: Springアノテーション・Exposed・HTTPへの依存（ゼロ依存）

#### Infrastructure層
- **責務**: Repositoryインターフェースの実装（Exposed DSL）、ドメインモデル ↔ DBレコードの変換、Flyway SQLスクリプトの管理
- **許可**: Domain層のインターフェースの実装、DBアクセス
- **禁止**: ビジネスルールの実装

---

## パッケージ構成

```
src/main/kotlin/com/example/ec/
├── http/                          # HTTP層
│   ├── controller/                #   REST Controller（リクエスト受付・UseCase呼び出し）
│   ├── presenter/                 #   Presenter（UseCaseレスポンスをHTTP用に整形）
│   ├── filter/                    #   JWT認証フィルター
│   └── advice/                    #   例外ハンドラー (@ExceptionHandler)
├── usecase/                       # UseCase層（詳細は repository-structure.md 参照）
│   ├── customer/                  #   顧客ユースケース（register/, login/ 等）
│   ├── product/                   #   商品ユースケース（list/, get/, register/ 等）
│   ├── cart/                      #   カートユースケース（get/, add/, remove/）
│   ├── favorite/                  #   お気に入りユースケース（get/, add/, remove/）
│   └── order/                     #   注文ユースケース（place/, get/, list/ 等）
├── domain/                        # Domain層
│   ├── customer/                  #   顧客コンテキスト
│   │   ├── Customer.kt            #     集約ルート
│   │   ├── Address.kt             #     値オブジェクト
│   │   ├── Email.kt               #     値オブジェクト
│   │   ├── Favorite.kt            #     集約ルート
│   │   ├── CustomerRepository.kt  #     Repositoryインターフェース
│   │   └── FavoriteRepository.kt  #     Repositoryインターフェース
│   ├── product/                   #   カタログコンテキスト
│   │   ├── Product.kt
│   │   ├── Category.kt
│   │   ├── Money.kt               #     値オブジェクト
│   │   ├── ProductRepository.kt
│   │   └── CategoryRepository.kt
│   └── order/                     #   注文コンテキスト
│       ├── Cart.kt
│       ├── Order.kt
│       ├── OrderItem.kt
│       ├── CartRepository.kt
│       └── OrderRepository.kt
└── infrastructure/                # Infrastructure層
    ├── repository/                #   Repository実装
    │   ├── CustomerRepositoryImpl.kt
    │   ├── FavoriteRepositoryImpl.kt
    │   ├── ProductRepositoryImpl.kt
    │   ├── CategoryRepositoryImpl.kt
    │   ├── CartRepositoryImpl.kt
    │   └── OrderRepositoryImpl.kt
    ├── table/                     #   ExposedテーブルDSL定義
    └── config/                    #   Spring設定 (Security, DB等)

src/main/resources/
├── db/migration/                  # Flyway SQLスクリプト
│   ├── V1__create_customers.sql
│   ├── V2__create_products.sql
│   └── ...
└── application.yml                # アプリケーション設定

src/test/kotlin/com/example/ec/
├── domain/                        # ドメイン層ユニットテスト
├── usecase/                       # UseCaseユニットテスト（MockK）
├── infrastructure/                # Repository統合テスト（TestContainers）
└── http/                          # HTTP層統合テスト（MockMvc）
```

---

## データ永続化戦略

### ストレージ方式

| データ種別 | ストレージ | 方式 |
|-----------|----------|------|
| 顧客・商品・注文など全ドメインデータ | PostgreSQL 16 | Exposed DSL（型安全なSQLクエリ） |
| DBスキーマ変更履歴 | Flyway | バージョン番号付きSQLファイル |

### Exposed DSLの使い方（方針）

ドメインオブジェクトとDBテーブルは明確に分離する。

```kotlin
// テーブル定義（Infrastructure層）
object CustomersTable : Table("customers") {
    val id = uuid("id")
    val name = varchar("name", 50)
    val email = varchar("email", 255)
    val passwordHash = varchar("password_hash", 255)
    val status = varchar("status", 20)
    override val primaryKey = PrimaryKey(id)
}

// Repositoryの実装（ドメイン ↔ DBの変換を行う）
class CustomerRepositoryImpl : CustomerRepository {
    override fun findById(id: CustomerId): Customer? =
        CustomersTable
            .select { CustomersTable.id eq id.value }
            .singleOrNull()
            ?.toDomain()           // DBレコード → ドメインオブジェクト変換

    private fun ResultRow.toDomain(): Customer = Customer(
        id = CustomerId(this[CustomersTable.id]),
        name = this[CustomersTable.name],
        email = Email(this[CustomersTable.email]),
        // ...
    )
}
```

### Flywayマイグレーション方針

- ファイル名: `V{バージョン}__{説明}.sql`（例: `V1__create_customers.sql`）
- 一度コミットしたマイグレーションファイルは変更しない（新しいバージョンで対応）
- ローカル・CI・本番で同一のマイグレーションを適用

---

## セキュリティアーキテクチャ

### 認証フロー（JWT Cookie）

```
1. POST /api/v1/auth/login
   → メールアドレス・パスワード検証
   → JWTを生成（ペイロード: customerId, exp）
   → Set-Cookie: jwt={token}; HttpOnly; SameSite=Strict; Path=/

2. POST /api/v1/auth/logout
   → jwt Cookie を maxAge=0 で上書きし無効化

3. 認証が必要なAPI
   → Cookie の jwt 値を検証
   → 署名・有効期限チェック（DBアクセスなし）
   → ペイロードからcustomerIdを取得しControllerに渡す
```

Cookie 方式採用の理由: `HttpOnly` により JavaScript からのトークン読み取りを防ぎ XSS 耐性を高める。`SameSite=Strict` により CSRF を抑制する。

### JWT設定値

| パラメータ | 値 | 備考 |
|-----------|-----|------|
| アルゴリズム | HS256 | 対称鍵署名 |
| 有効期限（exp） | 3600秒（1時間） | Cookie の maxAge にも同値を設定 |
| 署名鍵 | 環境変数 `JWT_SECRET` | ハードコード禁止 |

### 認可（認可制御）

- 自分以外の顧客のリソース（注文・カート・お気に入り）にはアクセス不可
- UseCase層でリソースの所有者チェックを行う

### データ保護

| 対象 | 方式 |
|------|------|
| パスワード | BCryptハッシュ（ソルト付き） |
| JWT署名鍵 | 環境変数で管理（ハードコード禁止） |
| SQLインジェクション | Exposed型安全DSLにより根本的に防止 |

---

## パフォーマンス要件

| 操作 | 目標時間 | 測定環境 |
|------|---------|---------|
| API通常操作 | 200ms以内 | ローカルdevcontainer環境 |
| 商品一覧（20件ページネーション） | 100ms以内 | 同上 |
| JWT検証 | 5ms以内 | 同上（DB照会なし） |

**測定前提条件**:
- JVM: ウォームアップ後（アプリ起動後10リクエスト以降）
- DBレコード数: 商品500件、顧客100件程度の状態
- 環境: devcontainer（Docker）上のPostgreSQL 16

### N+1対策

- カート・お気に入り一覧取得時は商品情報をJOINして1クエリで取得
- Exposed DSLの`join`を活用

---

## テスト戦略

| 層 | 種別 | ツール | カバレッジ目標 |
|---|---|---|---|
| Domain | ユニットテスト | JUnit 5 + MockK | 80%以上 |
| UseCase | ユニットテスト（Repositoryをモック） | JUnit 5 + MockK | 主要フロー100% |
| Infrastructure | 統合テスト（実DB） | TestContainers + PostgreSQL 16 | 全Repository実装 |
| HTTP | 統合テスト | MockMvc + UseCaseモック | 全エンドポイント |

---

## 技術的制約

### 環境要件
- **Java**: 21以上
- **Docker**: devcontainer使用のため必須
- **メモリ**: JVM起動のため最低512MB推奨

### スコープ外（今回の設計に含まない）
- Redis・セッションDB（JWTによりステートレス設計）
- メッセージキュー（将来のドメインイベント実装時に検討）
- CDN・画像ストレージ（画像アップロード機能はスコープ外）
- 本番デプロイ・クラウドインフラ

---

## 依存関係管理

```kotlin
// build.gradle.kts 方針
dependencies {
    // Spring Boot BOMでバージョン統一管理
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.x.x"))

    // Exposed: マイナーバージョン固定（破壊的変更が多いため）
    implementation("org.jetbrains.exposed:exposed-core:0.5x.x")

    // TestContainers: マイナーバージョンまで許可
    testImplementation("org.testcontainers:postgresql:1.+")
}
```

**方針**:
- Spring Boot BOMで依存バージョンを一元管理
- Exposedは破壊的変更が多いためバージョンを固定
- セキュリティ関連ライブラリ（JJWT等）はパッチリリースを随時適用
