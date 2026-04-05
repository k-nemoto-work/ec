# 設計書

## アーキテクチャ概要

クリーンアーキテクチャ（4層構成）に従い、顧客管理機能を実装する。

```
HTTP層: AuthController, CustomerController, JwtAuthenticationFilter, GlobalExceptionHandler
    ↓
UseCase層: RegisterCustomerUseCase, LoginUseCase, GetCustomerProfileUseCase, UpdateAddressUseCase
    ↓
Domain層: Customer, CustomerId, Email, Address, CustomerStatus, CustomerRepository（IF）
    ↑
Infrastructure層: CustomerRepositoryImpl, CustomersTable, AddressesTable, SecurityConfig, JwtConfig, JwtTokenProvider
```

## コンポーネント設計

### 1. Domain層 - 顧客コンテキスト

**ファイル構成**:
- `domain/customer/Customer.kt` - 集約ルート
- `domain/customer/CustomerId.kt` - 値オブジェクト
- `domain/customer/Email.kt` - 値オブジェクト（フォーマット検証付き）
- `domain/customer/Address.kt` - 値オブジェクト（不変）
- `domain/customer/CustomerStatus.kt` - 列挙型（ACTIVE, INACTIVE）
- `domain/customer/CustomerRepository.kt` - Repositoryインターフェース
- `domain/exception/DomainValidationException.kt` - ドメインバリデーション例外
- `domain/exception/BusinessRuleViolationException.kt` - ビジネスルール違反例外
- `domain/exception/ResourceNotFoundException.kt` - リソース未検出例外

**ビジネスルール**:
- Email: 空でない、@を含む、255文字以内
- パスワード: 8文字以上、英字と数字の両方を含む（検証はドメイン層、ハッシュ化はインフラ層）
- 顧客名: 1-50文字
- INACTIVEの顧客はログイン不可

### 2. UseCase層 - 顧客ユースケース

**RegisterCustomerUseCase**:
- Command: name, email, password
- メールアドレス重複チェック → パスワードハッシュ化 → Customer生成 → 保存
- パスワードハッシュ化はインターフェース経由（PasswordEncoder）

**LoginUseCase**:
- Command: email, password
- 顧客検索 → パスワード検証 → ステータスチェック → JWT生成
- Result: accessToken, expiresIn

**GetCustomerProfileUseCase**:
- customerId → 顧客取得 → プロフィール返却

**UpdateAddressUseCase**:
- Command: customerId, postalCode, prefecture, city, streetAddress
- 顧客取得 → Address値オブジェクト生成 → 顧客更新 → 保存

### 3. Infrastructure層

**CustomersTable**: Exposed DSLテーブル定義（id, name, email, password_hash, status, created_at, updated_at）
**AddressesTable**: Exposed DSLテーブル定義（customer_id, postal_code, prefecture, city, street_address）
**CustomerRepositoryImpl**: CustomerRepository実装（Exposed DSL）

**SecurityConfig**: Spring Security設定（JWT認証フィルター登録、パス別認可）
**JwtConfig**: JWT設定（秘密鍵、有効期限）
**JwtTokenProvider**: JWT生成・検証ユーティリティ

### 4. HTTP層

**AuthController**: POST /api/v1/auth/register, POST /api/v1/auth/login
**CustomerController**: GET /api/v1/customers/me, PUT /api/v1/customers/me/address
**JwtAuthenticationFilter**: リクエストヘッダーからJWT検証しSecurityContextに認証情報セット
**GlobalExceptionHandler**: ドメイン例外→HTTPステータスマッピング
**ErrorResponse**: 共通エラーレスポンス形式

## データフロー

### 会員登録
```
1. POST /api/v1/auth/register (name, email, password)
2. AuthController → RegisterCustomerUseCase.execute(command)
3. UseCase: customerRepository.findByEmail(email) → 重複チェック
4. UseCase: passwordEncoder.encode(password) → ハッシュ化
5. UseCase: Customer.create(name, email, passwordHash) → ドメインオブジェクト生成
6. UseCase: customerRepository.save(customer)
7. 201 Created { customerId: "uuid" }
```

### ログイン
```
1. POST /api/v1/auth/login (email, password)
2. AuthController → LoginUseCase.execute(command)
3. UseCase: customerRepository.findByEmail(email) → 顧客検索
4. UseCase: passwordEncoder.matches(password, customer.passwordHash) → パスワード検証
5. UseCase: customer.status == ACTIVE チェック
6. UseCase: jwtTokenProvider.generateToken(customerId) → JWT生成
7. 200 OK { accessToken: "jwt", expiresIn: 3600 }
```

## エラーハンドリング戦略

### カスタムエラークラス

| 例外クラス | HTTPステータス | 用途 |
|---|---|---|
| DomainValidationException | 400 | 値オブジェクトのバリデーション違反 |
| BusinessRuleViolationException | 409 | メールアドレス重複、非活性顧客のログイン |
| ResourceNotFoundException | 404 | 顧客が見つからない |

### エラーコード

| コード | 意味 |
|---|---|
| EMAIL_ALREADY_EXISTS | 登録済みのメールアドレス |
| INVALID_CREDENTIALS | メールアドレスまたはパスワードが正しくない |
| CUSTOMER_INACTIVE | 非活性の顧客がログインを試みた |
| CUSTOMER_NOT_FOUND | 顧客が見つからない |

## テスト戦略

### ユニットテスト（Domain層）
- Customer: 生成、ステータス検証
- Email: フォーマット検証（正常・異常）
- Address: 値オブジェクト生成
- パスワードバリデーション: 8文字以上、英数字混在

### ユニットテスト（UseCase層）
- RegisterCustomerUseCase: 正常登録、メール重複エラー
- LoginUseCase: 正常ログイン、不正パスワード、非活性顧客
- GetCustomerProfileUseCase: 正常取得、未登録顧客
- UpdateAddressUseCase: 正常更新

### 統合テスト（Infrastructure層）
- CustomerRepositoryImpl: CRUD操作、メールアドレスユニーク制約

## 依存ライブラリ

```kotlin
// build.gradle.kts に追加
implementation("org.springframework.boot:spring-boot-starter-security")
implementation("io.jsonwebtoken:jjwt-api:0.12.6")
runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
testImplementation("org.springframework.security:spring-security-test")
testImplementation("io.mockk:mockk:1.13.13")
testImplementation("org.testcontainers:postgresql:1.20.4")
testImplementation("org.testcontainers:junit-jupiter:1.20.4")
```

## ディレクトリ構造

```
src/main/kotlin/com/example/ec/
├── domain/
│   ├── customer/
│   │   ├── Customer.kt
│   │   ├── CustomerId.kt
│   │   ├── Email.kt
│   │   ├── Address.kt
│   │   ├── CustomerStatus.kt
│   │   └── CustomerRepository.kt
│   └── exception/
│       ├── DomainValidationException.kt
│       ├── BusinessRuleViolationException.kt
│       └── ResourceNotFoundException.kt
├── usecase/
│   └── customer/
│       ├── register/
│       │   ├── RegisterCustomerUseCase.kt
│       │   └── RegisterCustomerCommand.kt
│       ├── login/
│       │   ├── LoginUseCase.kt
│       │   ├── LoginCommand.kt
│       │   └── LoginResult.kt
│       ├── get_profile/
│       │   ├── GetCustomerProfileUseCase.kt
│       │   └── CustomerProfileResult.kt
│       └── update_address/
│           ├── UpdateAddressUseCase.kt
│           └── UpdateAddressCommand.kt
├── http/
│   ├── controller/
│   │   ├── AuthController.kt
│   │   └── CustomerController.kt
│   ├── presenter/
│   │   └── customer/
│   │       ├── RegisterCustomerPresenter.kt
│   │       ├── LoginPresenter.kt
│   │       ├── GetCustomerProfilePresenter.kt
│   │       └── UpdateAddressPresenter.kt
│   ├── filter/
│   │   └── JwtAuthenticationFilter.kt
│   └── advice/
│       ├── GlobalExceptionHandler.kt
│       └── ErrorResponse.kt
└── infrastructure/
    ├── repository/
    │   └── CustomerRepositoryImpl.kt
    ├── table/
    │   ├── CustomersTable.kt
    │   └── AddressesTable.kt
    └── config/
        ├── SecurityConfig.kt
        ├── JwtConfig.kt
        └── JwtTokenProvider.kt

src/main/resources/
└── db/migration/
    ├── V2__create_customers.sql
    └── V3__create_addresses.sql
```

## 実装の順序

1. Gradle依存関係の追加
2. Flywayマイグレーション（customers, addresses）
3. Domain層（値オブジェクト、集約ルート、Repository IF、例外クラス）
4. Domain層ユニットテスト
5. Infrastructure層（テーブルDSL、Repository実装、JWT、Security）
6. UseCase層（4ユースケース）
7. UseCase層ユニットテスト
8. HTTP層（Controller、Presenter、Filter、ExceptionHandler）
9. application.yml にJWT設定追加

## セキュリティ考慮事項

- パスワードはBCryptでハッシュ化（Spring SecurityのBCryptPasswordEncoder使用）
- JWT署名鍵は環境変数`JWT_SECRET`から取得（ハードコード禁止）
- JWTペイロードにはcustomerIdと有効期限のみ含める（パスワード等は含めない）
- 認証なしでアクセス可能なパスは `/api/v1/auth/**` と `/health` のみ

## パフォーマンス考慮事項

- JWT検証はDB照会なし（ステートレス）
- メールアドレスにユニークインデックスを設定（検索高速化）
