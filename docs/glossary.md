# プロジェクト用語集 (Glossary)

## 概要

このドキュメントは、ReUse Market プロジェクト内で使用される用語の定義を管理します。
ドメイン用語・アーキテクチャ用語・技術用語を統一的に定義し、コード・ドキュメント・会話における語彙のブレを防ぐ。

**更新日**: 2026-04-04

---

## ドメイン用語

### リユース品

**定義**: 一度使用された中古品。

**説明**: このシステムが扱う商品の種類。古着・家電・雑貨など。  
**重要な特性**: 同一商品が複数在庫として存在しない「1点もの」である。この特性がシステム設計の根幹にある。

**英語表記**: Used item / Second-hand item

---

### 顧客 (Customer)

**定義**: このシステムのユーザー。出品者兼管理者、または購入者として利用する。

**説明**: メールアドレスとパスワードで登録した会員。JWTで認証される。  
活性（ACTIVE）状態の顧客のみ注文・カート操作が可能。

**Kotlinクラス**: `Customer`（集約ルート）  
**関連用語**: 顧客ID、顧客ステータス、配送先住所  
**英語表記**: Customer

---

### 出品者

**定義**: 商品を出品・管理する顧客のロール。

**説明**: 独立したエンティティではなく、`Customer` がコンテキストに応じて担うロール。商品の登録・編集・ステータス変更を行う。

**英語表記**: Seller

---

### 購入者

**定義**: 商品を閲覧・購入する顧客のロール。

**説明**: 独立したエンティティではなく、`Customer` がコンテキストに応じて担うロール。商品の検索・カート操作・注文・お気に入り登録を行う。同一の `Customer` が出品者と購入者の両方のロールを担う。

**英語表記**: Buyer

---

### 顧客ID (CustomerId)

**定義**: 顧客を一意に識別するID。UUIDで生成される。

**Kotlinクラス**: `CustomerId(val value: UUID)`  
**英語表記**: CustomerId

---

### 配送先住所 (Address)

**定義**: 注文商品の配送先を表す値オブジェクト。郵便番号・都道府県・市区町村・番地で構成される。

**説明**: 不変な値オブジェクト。顧客に紐付いており、注文時にスナップショットとして注文に保存される。

**Kotlinクラス**: `Address`（値オブジェクト）  
**英語表記**: Address

---

### 商品 (Product)

**定義**: 出品された1点もののリユース品。

**説明**: 商品名・価格・説明・カテゴリ・ステータスを持つ。ステータスによって購入者への公開可否やカート追加可否が決まる。

**Kotlinクラス**: `Product`（集約ルート）  
**関連用語**: 商品ID、商品ステータス、カテゴリ、価格  
**英語表記**: Product

---

### 商品ID (ProductId)

**定義**: 商品を一意に識別するID。UUIDで生成される。

**Kotlinクラス**: `ProductId(val value: UUID)`  
**英語表記**: ProductId

---

### 価格 (Money)

**定義**: 商品の販売価格を表す値オブジェクト。単位は円（整数）。

**説明**: 0以下の値は設定できないというビジネスルールを内包する。  
注文確定時は注文明細にスナップショットとして保存され、価格変更の影響を受けない。

**Kotlinクラス**: `Money(val amount: Long)`  
**英語表記**: Money

---

### カテゴリ (Category)

**定義**: 商品を分類するためのラベル。例: 古着、家電、雑貨。

**Kotlinクラス**: `Category`、`CategoryId`  
**英語表記**: Category

---

### カート (Cart)

**定義**: 購入者が購入前に商品を一時的にまとめておく領域。

**説明**: 1顧客につき1つのカートのみ存在する。カート内の商品を注文確定することで注文（Order）が生成される。

**Kotlinクラス**: `Cart`（集約ルート）  
**関連用語**: カートアイテム  
**英語表記**: Cart

---

### カートアイテム (CartItem)

**定義**: カートに追加された1件の商品を表す要素。商品IDのみを持つ（価格スナップショットは注文確定時に `OrderItem` に記録される）。

**Kotlinクラス**: `CartItem`  
**英語表記**: CartItem

---

### お気に入り (Favorite)

**定義**: 購入者が気になる商品を保存するリスト。

**説明**: 1顧客につき1つのお気に入りリストが存在する。「販売中」または「売約済み」の商品のみ追加できる。

**Kotlinクラス**: `Favorite`（集約ルート）  
**関連用語**: お気に入りアイテム  
**英語表記**: Favorite

---

### 注文 (Order)

**定義**: カート内の商品を購入確定した記録。

**説明**: 注文確定後は内容を変更できない。注文明細には注文時点の商品名・価格がスナップショットとして保存される。決済（Payment）と配送（Shipment）を持つ。

**Kotlinクラス**: `Order`（集約ルート）  
**関連用語**: 注文ID、注文明細、注文ステータス、決済、配送  
**英語表記**: Order

---

### 注文明細 (OrderItem)

**定義**: 注文に含まれる1件の商品情報。注文時点の商品名・価格のスナップショットを保持する。

**説明**: 価格スナップショットを持つことで、商品価格が後から変更されても注文金額が変わらないことを保証する。

**Kotlinクラス**: `OrderItem`  
**英語表記**: OrderItem

---

### 決済 (Payment)

**定義**: 注文に紐付く決済の状態管理。本システムではモック実装（外部連携なし）。

**Kotlinクラス**: `Payment`  
**関連用語**: 決済ステータス、決済方法  
**英語表記**: Payment

---

### 配送 (Shipment)

**定義**: 注文に紐付く配送の状態管理。本システムではモック実装（外部連携なし）。

**Kotlinクラス**: `Shipment`  
**関連用語**: 配送ステータス  
**英語表記**: Shipment

---

## ステータス定義

### 商品ステータス (ProductStatus)

| ステータス | 英語 | 意味 |
|-----------|------|------|
| 販売中 | `ON_SALE` | 購入者が閲覧・カート追加できる状態 |
| 売約済み | `RESERVED` | 注文確定済みだが配送完了前の状態。閲覧可、購入不可 |
| 売却済み | `SOLD` | 配送完了した状態。閲覧・購入不可 |
| 非公開 | `PRIVATE` | 出品者が非公開設定した状態。閲覧・購入不可 |

**状態遷移**:
```
非公開 → 販売中 → 売約済み（注文確定時に自動遷移） → 売却済み（配送完了時に自動遷移）
販売中 → 非公開（出品者による手動操作）
```

---

### 注文ステータス (OrderStatus)

| ステータス | 英語 | 意味 |
|-----------|------|------|
| 未確定 | `PENDING` | 注文生成直後の初期状態 |
| 確定 | `CONFIRMED` | 注文が確定した状態 |
| 配送中 | `SHIPPING` | 商品が発送された状態（決済済み後のみ遷移可） |
| 配送完了 | `DELIVERED` | 商品が届いた状態 |
| キャンセル | `CANCELLED` | 注文がキャンセルされた状態（配送中・配送完了後は不可） |

**状態遷移**:
```
PENDING → CONFIRMED → SHIPPING（決済PAIDが条件） → DELIVERED
CONFIRMED → CANCELLED
※ SHIPPING以降はCANCELLEDへの遷移不可
```

---

### 決済ステータス (PaymentStatus)

| ステータス | 英語 | 意味 |
|-----------|------|------|
| 未決済 | `UNPAID` | 決済が完了していない状態 |
| 決済済み | `PAID` | 決済が完了した状態。配送ステータスを進める条件 |
| 返金済み | `REFUNDED` | 返金が完了した状態（**スコープ外**: 返金処理は本MVPスコープ外のため実装しない。[product-requirements.md](./product-requirements.md) 参照） |

**状態遷移**:
```
UNPAID → PAID
※ REFUNDED はスコープ外のため現時点では遷移なし
```

---

### 配送ステータス (ShipmentStatus)

| ステータス | 英語 | 意味 |
|-----------|------|------|
| 未発送 | `NOT_SHIPPED` | まだ発送していない状態 |
| 発送済み | `SHIPPED` | 発送した状態（決済済み後のみ遷移可） |
| 配送完了 | `DELIVERED` | 配送が完了した状態 |

**状態遷移**:
```
NOT_SHIPPED → SHIPPED（PaymentStatus=PAIDが条件） → DELIVERED
```

---

### 顧客ステータス (CustomerStatus)

| ステータス | 英語 | 意味 |
|-----------|------|------|
| 活性 | `ACTIVE` | ログイン・注文・カート操作が可能な状態 |
| 非活性 | `INACTIVE` | ログインできない状態 |

---

## アーキテクチャ用語

### DDD（ドメイン駆動設計）

**定義**: ビジネスドメインを中心に据えたソフトウェア設計手法。ドメインの複雑さをドメインモデルに正確に表現する。

**本プロジェクトでの適用**: 集約・値オブジェクト・リポジトリ・ドメインサービスなどのDDDパターンをKotlinで実装する。

**英語表記**: Domain-Driven Design (DDD)

---

### 集約 (Aggregate)

**定義**: DDDにおける一貫性の境界単位。集約ルートと呼ばれるエンティティを中心に、関連するオブジェクトをグループ化したもの。

**本プロジェクトでの適用**: `Customer`, `Favorite`, `Product`, `Cart`, `Order` が集約ルート。  
集約の外からは集約ルートのIDでのみ参照する。

**英語表記**: Aggregate

---

### 集約ルート (Aggregate Root)

**定義**: 集約の入口となる唯一のエンティティ。外部からは集約ルートを通じてのみ集約内のオブジェクトを操作する。

**本プロジェクトでの集約ルート**: `Customer`, `Favorite`, `Product`, `Cart`, `Order`

**英語表記**: Aggregate Root

---

### 値オブジェクト (Value Object)

**定義**: 同一性をIDではなく値で識別するオブジェクト。不変であり、ビジネスルールの検証を内包する。

**本プロジェクトでの適用**: `Email`, `Money`, `Address`, `CustomerId`, `ProductId` など。Kotlin の `data class` で表現する。

**例**:
```kotlin
data class Money(val amount: Long) {
    init {
        require(amount > 0) { "価格は0より大きい値でなければなりません" }
    }
}
```

**英語表記**: Value Object

---

### リポジトリ (Repository)

**定義**: 集約の永続化と取得を抽象化するインターフェース。Domain層で定義し、Infrastructure層で実装する。

**本プロジェクトでの適用**: `CustomerRepository`, `ProductRepository`, `CartRepository`, `OrderRepository`, `FavoriteRepository` など。

**英語表記**: Repository

---

### ユビキタス言語 (Ubiquitous Language)

**定義**: ドメインエキスパートと開発者が共通して使う言語。コード・ドキュメント・会話で同じ用語を使うことで認識のズレをなくす。

**本プロジェクトでの適用**: このGlossaryで定義する用語群がユビキタス言語にあたる。コードの変数名・クラス名・APIパスにも使用する。

**英語表記**: Ubiquitous Language

---

### 境界付けられたコンテキスト (Bounded Context)

**定義**: ドメインモデルの適用範囲の境界。コンテキストをまたいでドメインオブジェクトを直接参照しない。

**本プロジェクトのコンテキスト**:
- 顧客コンテキスト（Customer, Favorite）
- カタログコンテキスト（Product）
- 注文コンテキスト（Cart, Order）
- 決済コンテキスト（Payment）
- 配送コンテキスト（Shipment）

**英語表記**: Bounded Context

---

### クリーンアーキテクチャ (Clean Architecture)

**定義**: 関心事の分離を徹底した層状のアーキテクチャ。内側の層が外側の層に依存しないという依存ルールを持つ。

**本プロジェクトでの構成**:
```
HTTP層（外側）→ UseCase層 → Domain層（内側、ゼロ依存）
                               ↑
                Infrastructure層（依存逆転）
```

**英語表記**: Clean Architecture

---

### ユースケース (UseCase)

**定義**: システムが提供する1つの機能単位。1クラス1ユースケースで実装する。トランザクション境界を管理する。

**本プロジェクトでの適用**: `PlaceOrderUseCase`, `AddToCartUseCase`, `RegisterCustomerUseCase` など。`execute()` メソッドで実行する。

**英語表記**: UseCase

---

### コマンド (Command)

**定義**: UseCaseへの入力データを表すオブジェクト。書き込み系操作（登録・更新・削除）の入力に使用する。

**本プロジェクトでの適用**: `PlaceOrderCommand`, `AddToCartCommand`, `RegisterCustomerCommand` など。  
Kotlin の `data class` で表現する。

**英語表記**: Command

---

### クエリ (Query)

**定義**: UseCaseへの検索条件を表すオブジェクト。読み取り系操作の入力に使用する。

**本プロジェクトでの適用**: `ListProductsQuery` など。

**英語表記**: Query

---

### プレゼンター (Presenter)

**定義**: UseCaseの出力（Result）をHTTPレスポンス用のJSON構造に整形するオブジェクト。ユースケース単位で作成する。

**本プロジェクトでの適用**: `GetCartPresenter`, `PlaceOrderPresenter` など。

**英語表記**: Presenter

---

### スナップショット (Snapshot)

**定義**: 特定の時点でのデータの値をそのまま保存したもの。参照元のデータが変更されても影響を受けない。

**本プロジェクトでの適用**: 注文明細（OrderItem）に注文時点の商品名・価格を保存すること。商品価格が後から変更されても注文金額は変わらない。

**英語表記**: Snapshot

---

## エラー・例外

### DomainValidationException

**クラス名**: `DomainValidationException`

**発生条件**: 値の形式・制約違反。空文字、0以下の金額、不正なメールアドレス形式など。

**HTTPステータス**: 400 Bad Request

**例**:
```kotlin
throw DomainValidationException("メールアドレスの形式が不正です: $value")
```

---

### BusinessRuleViolationException

**クラス名**: `BusinessRuleViolationException`

**発生条件**: ビジネスルール違反。売約済み商品へのカート追加、配送中の注文のキャンセルなど。

**HTTPステータス**: 409 Conflict

**例**:
```kotlin
throw BusinessRuleViolationException("販売中の商品のみカートに追加できます")
```

---

### ResourceNotFoundException

**クラス名**: `ResourceNotFoundException`

**発生条件**: 指定したIDのリソースが存在しない場合。

**HTTPステータス**: 404 Not Found

**例**:
```kotlin
throw ResourceNotFoundException("Product", productId.value.toString())
```

---

### UnauthorizedAccessException

**クラス名**: `UnauthorizedAccessException`

**発生条件**: 他の顧客のリソース（注文・カート・お気に入り）にアクセスしようとした場合。

**HTTPステータス**: 403 Forbidden

**例**:
```kotlin
throw UnauthorizedAccessException("このリソースにアクセスする権限がありません")
```

---

## 技術用語

### Exposed

**定義**: JetBrains製のKotlinネイティブORMライブラリ。型安全なSQL DSLとDAOの2つのAPIを提供する。

**本プロジェクトでの用途**: DSL APIを使用してSQL文を型安全に記述する。Infrastructure層のRepository実装で使用する。

**バージョン**: 0.5x.x

**関連ドキュメント**: [architecture.md](./architecture.md)

---

### Flyway

**定義**: SQLファイルベースのDBマイグレーション管理ツール。バージョン番号付きSQLファイルでスキーマ変更履歴を管理する。

**本プロジェクトでの用途**: `src/main/resources/db/migration/V{N}__{説明}.sql` ファイルでスキーマを管理する。一度コミットしたファイルは変更しない。

**バージョン**: 10.x

---

### TestContainers

**定義**: テスト実行時にDockerコンテナを自動起動してテスト環境を構築するライブラリ。

**本プロジェクトでの用途**: Infrastructure層の統合テストでPostgreSQL 16コンテナを起動し、実際のDBに対してRepositoryの動作を検証する。

**バージョン**: 1.x

---

### MockK

**定義**: Kotlin向けのモックライブラリ。Kotlinのdata classや`object`に対応したモック生成が可能。

**本プロジェクトでの用途**: UseCase層のユニットテストでRepositoryインターフェースをモック化するために使用する。

**バージョン**: 1.x

---

### JWT (JSON Web Token)

**正式名称**: JSON Web Token

**定義**: JSON形式のクレームを含む自己完結型のトークン。署名（または暗号化）されており、検証のためにDBアクセスが不要。

**本プロジェクトでの使用**: ログイン成功後にサーバーがJWTを発行する。以降のリクエストでは `Authorization: Bearer {token}` ヘッダーに含めて送信する。ペイロードには `customerId` と有効期限を含む。

---

### BCrypt

**定義**: パスワードをソルト付きでハッシュ化するアルゴリズム。

**本プロジェクトでの使用**: 顧客のパスワードをDBに保存する前にBCryptでハッシュ化する。Spring Security内包のBCryptPasswordEncoderを使用。

---

### SpringDoc OpenAPI

**定義**: Spring BootアプリケーションからOpenAPI 3.0仕様のAPIドキュメントを自動生成するライブラリ。

**本プロジェクトでの使用**: Controller・RequestBody・ResponseBodyのアノテーションからSwagger UIを自動生成する。エンドポイント: `http://localhost:8080/swagger-ui.html`

**バージョン**: 2.x

---

## 略語

### DDD

**正式名称**: Domain-Driven Design

**意味**: ドメイン駆動設計

**本プロジェクトでの使用**: このプロジェクト全体のアーキテクチャ設計思想の根幹。

---

### ORM

**正式名称**: Object-Relational Mapping

**意味**: オブジェクトとリレーショナルDBのテーブルをマッピングする技術

**本プロジェクトでの使用**: ExposedをORMとして使用。

---

### DIP

**正式名称**: Dependency Inversion Principle

**意味**: 依存性逆転の原則。上位モジュールは下位モジュールに依存してはならず、両者とも抽象に依存すべきという原則。

**本プロジェクトでの使用**: Domain層がRepositoryインターフェースを定義し、Infrastructure層がそれを実装する。UseCase層はインターフェースのみに依存することで、DBの詳細への依存を排除する。

---

### JWT

**正式名称**: JSON Web Token

**意味**: JSON形式の自己完結型トークン

**本プロジェクトでの使用**: 認証トークンとして使用。→ 詳細は「技術用語 > JWT」を参照。

---

### MVP

**正式名称**: Minimum Viable Product

**意味**: 最小限の機能を持つプロダクト

**本プロジェクトでの使用**: 5集約（顧客・商品・カート・お気に入り・注文）の実装完了がMVP。
