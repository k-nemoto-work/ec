# タスクリスト

## 🚨 タスク完全完了の原則

**このファイルの全タスクが完了するまで作業を継続すること**

### 必須ルール
- **全てのタスクを`[x]`にすること**
- 「時間の都合により別タスクとして実施予定」は禁止
- 「実装が複雑すぎるため後回し」は禁止
- 未完了タスク（`[ ]`）を残したまま作業を終了しない

### タスクスキップが許可される唯一のケース
技術的理由に該当する場合のみスキップ可能。スキップ時は必ず理由を明記。

---

## フェーズ1: 基盤準備

- [x] build.gradle.ktsに依存ライブラリを追加（Spring Security, JJWT, MockK, TestContainers）
- [x] application.ymlにJWT設定を追加
- [x] Flywayマイグレーション V2__create_customers.sql を作成
- [x] Flywayマイグレーション V3__create_addresses.sql を作成

## フェーズ2: Domain層

- [x] DomainValidationException を作成
- [x] BusinessRuleViolationException を作成
- [x] ResourceNotFoundException を作成
- [x] CustomerId 値オブジェクトを作成
- [x] Email 値オブジェクトを作成（フォーマット検証付き）
- [x] Address 値オブジェクトを作成
- [x] CustomerStatus 列挙型を作成
- [x] Customer 集約ルートを作成（パスワードバリデーション含む）
- [x] CustomerRepository インターフェースを作成

## フェーズ3: Domain層ユニットテスト

- [x] EmailTest: 正常なメールアドレスの生成、不正な形式でエラー
- [x] CustomerTest: 正常な顧客の生成、パスワードバリデーション（8文字以上、英数字混在）
- [x] AddressTest: 正常な住所の生成

## フェーズ4: Infrastructure層

- [x] CustomersTable（Exposed DSL）を作成
- [x] AddressesTable（Exposed DSL）を作成
- [x] CustomerRepositoryImpl を作成（save, findById, findByEmail, updateAddress）
- [x] JwtConfig を作成（秘密鍵・有効期限の設定）
- [x] JwtTokenProvider を作成（JWT生成・検証）
- [x] SecurityConfig を作成（Spring Security設定、JWT認証フィルター登録）
- [x] JwtAuthenticationFilter を作成

## フェーズ5: UseCase層

- [x] RegisterCustomerCommand を作成
- [x] RegisterCustomerUseCase を作成
- [x] LoginCommand, LoginResult を作成
- [x] LoginUseCase を作成
- [x] CustomerProfileResult を作成
- [x] GetCustomerProfileUseCase を作成
- [x] UpdateAddressCommand を作成
- [x] UpdateAddressUseCase を作成

## フェーズ6: UseCase層ユニットテスト

- [x] RegisterCustomerUseCaseTest: 正常登録、メール重複エラー
- [x] LoginUseCaseTest: 正常ログイン、不正パスワード、非活性顧客
- [x] GetCustomerProfileUseCaseTest: 正常取得、未登録顧客
- [x] UpdateAddressUseCaseTest: 正常更新

## フェーズ7: HTTP層

- [x] ErrorResponse を作成（共通エラーレスポンス形式）
- [x] GlobalExceptionHandler を作成（ドメイン例外→HTTPステータスマッピング）
- [x] AuthController を作成（POST /api/v1/auth/register, POST /api/v1/auth/login）
- [x] CustomerController を作成（GET /api/v1/customers/me, PUT /api/v1/customers/me/address）

## フェーズ8: ビルド確認・修正

- [x] `./gradlew compileKotlin` でコンパイルが成功することを確認
- [x] `./gradlew test` で全テストがパスすることを確認
- [x] コンパイルエラー・テスト失敗があれば修正

---

## 実装後の振り返り

### 実装完了日
2026-04-04

### 計画と実績の差分

**計画と異なった点**:
- `@MockK` アノテーション（MockKExtension 経由）が MockK 1.13.13 では機能しなかった。`mockk()` 関数を使ったプロパティ初期化に変更

**新たに必要になったタスク**:
- テストファイル3件の `@MockK` → `mockk()` 関数への修正（LoginUseCaseTest, RegisterCustomerUseCaseTest, UpdateAddressUseCaseTest）

### 学んだこと

**技術的な学び**:
- MockK 1.13.13 では `@MockK` アノテーションが `io.mockk.MockK` クラスと名前衝突するため、`@ExtendWith(MockKExtension::class)` + `@MockK` の組み合わせよりも `mockk()` 関数での直接初期化が安全

**プロセス上の改善点**:
- テスト実装時にアノテーションベースのモック注入を使う場合は、ライブラリバージョンとの互換性を事前に確認する

### 次回への改善提案
- MockK を使う場合は `private val foo: FooClass = mockk()` パターンで統一する
