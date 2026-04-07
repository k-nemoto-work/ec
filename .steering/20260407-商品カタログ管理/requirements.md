# 要求定義: 商品カタログ管理

## 概要

出品者が商品を登録・管理し、購入者が商品を閲覧できるカタログ機能を実装する。

## ユーザーストーリー

出品者として、商品を登録・公開管理するために、商品情報のCRUDと公開ステータスの管理がしたい

## 受け入れ条件

- [ ] 商品名・価格・説明・カテゴリを指定して商品を登録できる（商品名は1〜100文字、説明は2000文字以内）
- [ ] 商品ステータスを「販売中(ON_SALE) / 売約済み(RESERVED) / 売却済み(SOLD) / 非公開(PRIVATE)」で管理できる
- [ ] 購入者は「販売中(ON_SALE)」ステータスの商品のみ一覧・詳細を閲覧できる
- [ ] 商品をカテゴリで絞り込みできる
- [ ] 価格は0より大きい値でなければ登録できない
- [ ] 商品一覧はページネーション対応（デフォルト20件）

## APIエンドポイント

| メソッド | パス | 説明 | 認証 |
|---|---|---|---|
| GET | `/api/v1/products` | 商品一覧（ON_SALEのみ、カテゴリ絞り込み・ページネーション対応） | 不要 |
| GET | `/api/v1/products/{productId}` | 商品詳細（ON_SALEのみ） | 不要 |
| GET | `/api/v1/products/{productId}/management` | 商品詳細（全ステータス・出品者向け） | 必要 |
| POST | `/api/v1/products` | 商品登録（出品者） | 必要 |
| PUT | `/api/v1/products/{productId}` | 商品情報更新 | 必要 |
| PATCH | `/api/v1/products/{productId}/status` | 商品ステータス変更 | 必要 |

## ドメインモデル

### Product（集約ルート）
- id: ProductId (UUID)
- name: ProductName (1〜100文字)
- price: Money (0より大きい)
- description: String (2000文字以内)
- categoryId: CategoryId
- status: ProductStatus

### Category（エンティティ）
- id: CategoryId (UUID)
- name: String

### Money（値オブジェクト）
- amount: Long (1以上)

### ProductStatus（列挙型）
- ON_SALE（販売中）
- RESERVED（売約済み）
- SOLD（売却済み）
- PRIVATE（非公開）
