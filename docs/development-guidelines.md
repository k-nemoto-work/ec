# 開発ガイドライン (Development Guidelines)

## コーディング規約

### 命名規則

#### Kotlin 基本規則

| 種別 | 規則 | 例 |
|------|------|-----|
| クラス・オブジェクト | PascalCase | `Customer`, `PlaceOrderUseCase` |
| 関数・変数 | camelCase | `findById`, `customerId` |
| 定数（companion object） | UPPER_SNAKE_CASE | `MAX_NAME_LENGTH` |
| パッケージ | 全て小文字 | `com.example.ec.domain.customer` |
| Boolean変数 | `is`, `has`, `can` で始める | `isActive`, `hasItems` |

#### プロジェクト固有の命名規則

詳細は [repository-structure.md](./repository-structure.md) のファイル配置規則を参照してください。

```kotlin
// ✅ 良い例: UseCase は動詞で始める
class PlaceOrderUseCase
class GetCartUseCase
class AddToFavoriteUseCase

// ✅ 良い例: 値オブジェクトはドメイン語彙を使う
class Email(val value: String)
class Money(val amount: Long)
class CustomerId(val value: UUID)

// ✅ 良い例: Repository インターフェースは Repository サフィックス
interface CustomerRepository
interface OrderRepository

// ❌ 悪い例: 実装クラスは直接使わない（DI で注入する）
// val repo = CustomerRepositoryImpl()
```

### コードフォーマット

**インデント**: 4スペース（Kotlin標準）

**行の長さ**: 最大120文字

**ブロック関数 vs 式関数**:

```kotlin
// ✅ 短く書ける場合は式関数
fun CustomerId.toUUID(): UUID = value

// ✅ ロジックがある場合はブロック関数
fun Customer.activate(): Customer {
    check(status == CustomerStatus.INACTIVE) { "既に活性化済みです" }
    return copy(status = CustomerStatus.ACTIVE)
}
```

**data class の活用**:

```kotlin
// ✅ 値オブジェクトは data class で表現
data class Email(val value: String) {
    init {
        require(value.isNotBlank()) { "メールアドレスは空にできません" }
        require(value.contains("@")) { "メールアドレスの形式が不正です: $value" }
    }
}

// ✅ 不変なコマンドは data class
data class PlaceOrderCommand(
    val customerId: CustomerId,
    val shippingAddress: Address,
)
```

### コメント規約

**KDoc（ドキュメントコメント）**:

ドメインロジックの複雑なルールには KDoc を記載してください。

```kotlin
/**
 * カートに商品を追加する。
 *
 * 同一商品の重複追加は許可しない。
 * 「販売中」ステータスの商品のみ追加できる。
 *
 * @throws DomainValidationException 商品が既にカートに存在する場合
 * @throws BusinessRuleViolationException 商品が販売中でない場合
 */
fun Cart.addItem(product: Product): Cart { ... }
```

**インラインコメント**:

```kotlin
// ✅ 良い例: なぜそうするかを説明
// 注文確定時点の価格をスナップショットとして保存（価格変更の影響を受けない）
val orderItem = OrderItem(
    productId = product.id,
    productName = product.name,
    price = product.price,
)

// ❌ 悪い例: コードを読めば分かる内容
// cartItems をループする
for (item in cartItems) { ... }
```

---

## アーキテクチャ規約

### 層の依存ルール（必須）

```
http/ → usecase/ → domain/
                      ↑
         infrastructure/ （DIによる依存逆転）
```

| 層 | 依存可能 | 依存禁止 |
|---|---|---|
| http/ | usecase/ | domain/, infrastructure/ |
| usecase/ | domain/（インターフェースのみ） | infrastructure/, http/ |
| domain/ | なし（ゼロ依存） | 全ての外部ライブラリ・Spring |
| infrastructure/ | domain/ | usecase/, http/ |

**チェック方法**: Domain層のファイルに `import org.springframework` や `import org.jetbrains.exposed` が含まれていたら違反です。

### Domain層の実装ルール

```kotlin
// ✅ 良い例: ビジネスルールはドメインオブジェクトに集約
data class Cart(
    val id: CartId,
    val customerId: CustomerId,
    val items: List<CartItem>,
) {
    fun addItem(product: Product): Cart {
        // ビジネスルールをここに書く
        if (items.any { it.productId == product.id }) {
            throw DomainValidationException("商品は既にカートに追加されています: ${product.id}")
        }
        if (product.status != ProductStatus.ON_SALE) {
            throw BusinessRuleViolationException("販売中の商品のみカートに追加できます")
        }
        return copy(items = items + CartItem(productId = product.id, price = product.price))
    }
}

// ❌ 悪い例: ビジネスルールをUseCase層に書く
class AddToCartUseCase(...) {
    fun execute(command: AddToCartCommand) {
        val cart = cartRepository.findByCustomerId(command.customerId) ?: Cart.empty(command.customerId)
        val product = productRepository.findById(command.productId)
            ?: throw ResourceNotFoundException("Product", command.productId.value.toString())

        // ❌ このロジックはDomain層に移動すべき
        if (cart.items.any { it.productId == command.productId }) {
            throw DomainValidationException("商品は既にカートに追加されています")
        }
        ...
    }
}
```

### UseCase層の実装ルール

```kotlin
// ✅ 良い例: 1 UseCase = 1クラス、トランザクション管理はUseCase層
@Service
@Transactional
class PlaceOrderUseCase(
    private val cartRepository: CartRepository,
    private val productRepository: ProductRepository,
    private val orderRepository: OrderRepository,
) {
    fun execute(command: PlaceOrderCommand): PlaceOrderResult {
        val cart = cartRepository.findByCustomerId(command.customerId)
            ?: throw ResourceNotFoundException("Cart", command.customerId.value.toString())

        // ドメインオブジェクトに処理を委譲
        val (order, updatedProducts) = cart.placeOrder(command.shippingAddress)

        // Repositoryで永続化
        updatedProducts.forEach { productRepository.save(it) }
        orderRepository.save(order)

        return PlaceOrderResult(orderId = order.id)
    }
}
```

### エラーハンドリング規約

**例外の種類と使い分け**:

| 例外クラス | 用途 | HTTPステータス |
|-----------|------|--------------|
| `DomainValidationException` | 値の形式・制約違反（空文字、範囲外など） | 400 Bad Request |
| `BusinessRuleViolationException` | ビジネスルール違反（売約済み商品への操作など） | 409 Conflict |
| `ResourceNotFoundException` | リソースが存在しない | 404 Not Found |
| `UnauthorizedAccessException` | 他の顧客のリソースへのアクセス | 403 Forbidden |

```kotlin
// ✅ 良い例: 具体的なメッセージ付きで例外をスロー
throw BusinessRuleViolationException(
    "配送中・配送完了後は注文をキャンセルできません。現在のステータス: ${order.status}"
)

// ✅ 良い例: Kotlin の require / check を活用
data class Money(val amount: Long) {
    init {
        require(amount > 0) { "価格は0より大きい値でなければなりません: $amount" }
    }
}
```

---

## テスト規約

### テスト構成

詳細は [architecture.md](./architecture.md) のテスト戦略を参照してください。

| 層 | 種別 | ツール | カバレッジ目標 |
|---|---|---|---|
| Domain | ユニットテスト | JUnit 5 | 80%以上 |
| UseCase | ユニットテスト（Repositoryをモック） | JUnit 5 + MockK | 書き込み系UseCase（PlaceOrder, AddToCart等）の正常系・主要異常系100% |
| Infrastructure | 統合テスト（実DB） | TestContainers + PostgreSQL | 全Repository実装 |
| HTTP | 統合テスト | MockMvc + UseCaseモック | 全エンドポイント |

### テストの書き方

**テスト名は日本語で、条件と期待結果を明確に**:

```kotlin
// ✅ 良い例: バッククォートで日本語テスト名
@Test
fun `販売中の商品をカートに追加できる`() { ... }

@Test
fun `売約済みの商品はカートに追加できない`() { ... }

@Test
fun `既にカートにある商品を重複追加するとエラーになる`() { ... }

// ❌ 悪い例
@Test
fun test1() { ... }

@Test
fun addItem() { ... }
```

**Given-When-Then パターン**:

```kotlin
@Test
fun `販売中の商品をカートに追加できる`() {
    // Given
    val product = Product(
        id = ProductId(UUID.randomUUID()),
        name = "テスト商品",
        price = Money(1000),
        status = ProductStatus.ON_SALE,
    )
    val cart = Cart(
        id = CartId(UUID.randomUUID()),
        customerId = CustomerId(UUID.randomUUID()),
        items = emptyList(),
    )

    // When
    val updatedCart = cart.addItem(product)

    // Then
    assertThat(updatedCart.items).hasSize(1)
    assertThat(updatedCart.items.first().productId).isEqualTo(product.id)
}
```

**UseCase テストでの MockK 使用**:

```kotlin
@ExtendWith(MockKExtension::class)
class AddToCartUseCaseTest {
    @MockK lateinit var cartRepository: CartRepository
    @MockK lateinit var productRepository: ProductRepository

    private lateinit var useCase: AddToCartUseCase

    @BeforeEach
    fun setUp() {
        useCase = AddToCartUseCase(cartRepository, productRepository)
    }

    @Test
    fun `正常にカートに商品を追加できる`() {
        // Given
        val customerId = CustomerId(UUID.randomUUID())
        val productId = ProductId(UUID.randomUUID())
        val product = Product(id = productId, status = ProductStatus.ON_SALE, ...)
        val cart = Cart(customerId = customerId, items = emptyList(), ...)

        every { cartRepository.findByCustomerId(customerId) } returns cart
        every { productRepository.findById(productId) } returns product
        every { cartRepository.save(any()) } just Runs

        // When
        useCase.execute(AddToCartCommand(customerId, productId))

        // Then
        verify { cartRepository.save(any()) }
    }
}
```

**Infrastructure テストでの TestContainers 使用**:

```kotlin
abstract class AbstractRepositoryTest {
    companion object {
        @Container
        val postgres = PostgreSQLContainer("postgres:16")
            .withDatabaseName("test_db")

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }
}
```

---

## Git 運用ルール

### ブランチ戦略

```
main
  ├─ feature/add-order-domain       # 新機能
  ├─ fix/cart-duplicate-check       # バグ修正
  └─ refactor/customer-aggregate    # リファクタリング
```

| ブランチ種別 | 命名規則 | 用途 |
|------------|---------|------|
| `main` | - | 常に動作する状態を維持 |
| `feature/*` | `feature/[機能名]` | 新機能開発 |
| `fix/*` | `fix/[修正内容]` | バグ修正 |
| `refactor/*` | `refactor/[対象]` | リファクタリング |
| `docs/*` | `docs/[対象]` | ドキュメント更新 |

### コミットメッセージ規約

**フォーマット**:

```
<type>(<scope>): <subject>

<body>（オプション）

<footer>（オプション）
```

**Type**:

| type | 用途 |
|------|------|
| `feat` | 新機能 |
| `fix` | バグ修正 |
| `docs` | ドキュメントのみの変更 |
| `refactor` | リファクタリング（機能変更なし） |
| `test` | テストの追加・修正 |
| `chore` | ビルド設定・依存関係更新等 |

**scope（対象）の例**: `domain`, `usecase`, `http`, `infra`, `cart`, `order`, `customer`, `product`

**例**:

```
feat(domain): Cart集約にaddItemメソッドを追加

販売中商品のみカートに追加できるビジネスルールを実装。
同一商品の重複追加防止チェックも含む。

Closes #12
```

```
test(usecase): AddToCartUseCaseの正常系・異常系テストを追加
```

### プルリクエストプロセス

**作成前のチェック**:
- [ ] 全てのテストがパス（`./gradlew test`）
- [ ] Domain層に外部ライブラリへの依存が混入していない
- [ ] コミットメッセージが規約に従っている

**PRテンプレート**:

```markdown
## 概要
[変更内容の簡潔な説明]

## 変更理由
[なぜこの変更が必要か]

## 変更内容
- [変更点1]
- [変更点2]

## テスト
- [ ] ドメインユニットテスト追加
- [ ] UseCaseユニットテスト追加
- [ ] 統合テスト追加（該当する場合）

## 関連Issue
Closes #[Issue番号]
```

---

## コードレビュー基準

### レビューポイント

**アーキテクチャ（最優先）**:
- [ ] 層の依存ルールが守られているか（Domain層にSpringが混入していないか）
- [ ] ビジネスルールがDomain層に集約されているか
- [ ] UseCase が適切にトランザクション境界を管理しているか

**ドメインモデルの正確性**:
- [ ] 値オブジェクトの不変条件（init ブロック）が適切か
- [ ] 集約の整合性が保たれているか
- [ ] エラーの種類（DomainValidationException / BusinessRuleViolationException）が適切か

**コード品質**:
- [ ] 命名がドメイン語彙（[glossary.md](./glossary.md)）に沿っているか
- [ ] テストが Given-When-Then パターンで書かれているか
- [ ] エッジケースがテストでカバーされているか

### レビューコメントの書き方

**優先度の明示**:
- `[必須]`: 設計原則違反、バグ、セキュリティ問題
- `[推奨]`: 保守性・可読性の改善
- `[提案]`: 代替案の提示（採用は任意）
- `[質問]`: 理解のための確認

```markdown
// ✅ 良い例
[必須] このロジックはUseCase層にありますが、ビジネスルールなのでDomain層のCart集約に移動してください。
Cart.addItem() にカプセル化することで、テストも書きやすくなります。

// ❌ 悪い例
この書き方は良くないです。
```

---

## 開発環境セットアップ

### 必要なツール

| ツール | バージョン | 用途 |
|--------|-----------|------|
| Docker | 最新 | devcontainer 実行 |
| VS Code または IntelliJ IDEA | 最新 | IDE |
| Dev Containers 拡張（VS Code の場合） | 最新 | devcontainer サポート |

### セットアップ手順

```bash
# 1. リポジトリのクローン
git clone <URL>
cd ec

# 2. devcontainer を開く（VS Code）
code .
# → "Reopen in Container" を選択

# 3. 環境変数の設定（JWT秘密鍵等）
cp application-local.yml.example src/main/resources/application-local.yml
# application-local.yml を編集

# 4. DBマイグレーション実行（devcontainer 内）
./gradlew flywayMigrate

# 5. アプリケーション起動
./gradlew bootRun

# 6. テスト実行
./gradlew test
```

### よく使うコマンド

```bash
# テスト実行（全て）
./gradlew test

# テスト実行（特定のクラス）
./gradlew test --tests "com.example.ec.domain.order.CartTest"

# ビルド
./gradlew build

# Flyway マイグレーション
./gradlew flywayMigrate

# Swagger UI（起動後）
open http://localhost:8080/swagger-ui.html
```

---

## 実装チェックリスト

### 新機能実装前
- [ ] [product-requirements.md](./product-requirements.md) で要件を確認
- [ ] [functional-design.md](./functional-design.md) で機能仕様を確認
- [ ] [glossary.md](./glossary.md) でドメイン語彙を確認
- [ ] 既存の類似実装を Grep で検索して、パターンを把握する

### 実装中
- [ ] Domain層にビジネスルールを集約している
- [ ] Domain層に Spring / Exposed が混入していない
- [ ] 値オブジェクトの不変条件を `init` / `require` で表現している
- [ ] 例外の種類が適切（DomainValidation / BusinessRule / NotFound / Unauthorized）

### テスト
- [ ] Domain ユニットテストが書かれている（カバレッジ80%以上）
- [ ] テスト名が日本語で「条件」と「期待結果」を表している
- [ ] 正常系・異常系（エッジケース）をカバーしている
- [ ] UseCase テストで MockK を使ってRepositoryをモックしている

### コミット前
- [ ] `./gradlew test` が全てパスする
- [ ] `./gradlew ktlintCheck` でフォーマット違反がない（違反がある場合は `./gradlew ktlintFormat` で自動修正）
- [ ] コミットメッセージが規約（type/scope/subject）に従っている
- [ ] Flyway マイグレーションファイルを一度コミット後に変更していない
