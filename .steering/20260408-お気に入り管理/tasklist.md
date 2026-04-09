# タスクリスト: お気に入り管理

## フェーズ1: Domain層

- [x] FavoriteId.kt を作成
- [x] FavoriteItem.kt を作成
- [x] Favorite.kt を作成（addItem/removeItem メソッド含む）
- [x] FavoriteRepository.kt インターフェースを作成

## フェーズ2: Infrastructure層

- [x] V6__create_favorites.sql を作成（favorites + favorite_items テーブル）
- [x] FavoritesTable.kt を作成（Exposed テーブル定義）
- [x] FavoriteItemsTable.kt を作成（Exposed テーブル定義）
- [x] FavoriteRepositoryImpl.kt を作成（findByCustomerId, save, update）

## フェーズ3: UseCase層

- [x] GetFavoriteUseCase.kt + FavoriteResult.kt + FavoriteItemResult.kt を作成
- [x] AddToFavoriteUseCase.kt + AddToFavoriteCommand.kt を作成
- [x] RemoveFromFavoriteUseCase.kt + RemoveFromFavoriteCommand.kt を作成

## フェーズ4: HTTP層

- [x] FavoriteController.kt を作成（GET/POST/DELETE エンドポイント）
- [x] SecurityConfig にお気に入りエンドポイントの認証設定を追加（anyRequest().authenticated() で自動カバーされるが確認）

## フェーズ5: テスト

- [x] FavoriteTest.kt を作成（Domain ユニットテスト）
- [x] GetFavoriteUseCaseTest.kt を作成
- [x] AddToFavoriteUseCaseTest.kt を作成
- [x] RemoveFromFavoriteUseCaseTest.kt を作成

## フェーズ6: ビルド・テスト確認

- [x] `./gradlew build` を実行してビルドが通ること確認
- [x] `./gradlew test` を実行して全テストがパスすること確認
- [x] 検証サブエージェントの指摘を修正（N+1解消・ホワイトリスト方式・NULL UUID修正・テスト追加）

---

## 実装後の振り返り

### 実装完了日
2026-04-08

### 計画と実績の差分

- `ProductRepository.findAllByIds()` の追加が必要だった（設計書に記載なかったが N+1 対策として追加）
- `FavoriteResult.favoriteId` を `UUID?` に変更（未作成時に架空のUUIDを返す問題を修正）
- `Favorite.addItem()` のステータスチェックをブラックリスト方式からホワイトリスト方式に変更（安全性向上）
- 検証後に `AddToFavoriteUseCaseTest` に PRIVATE 商品テストを追加

### 学んだこと

- ブラックリスト方式は `ProductStatus` に新ステータスが追加されたとき自動的に追加可能になるリスクがある。ホワイトリスト方式が安全
- UseCase のモックテストは実装変更（`findById` → `findAllByIds`）に追従する必要があるため、インターフェース変更時はテストも同時に更新すること

### 次回への改善提案

- Infrastructure 層（`FavoriteRepositoryImpl`）の TestContainers 統合テストは今回スコープ外だが、本番品質のためには追加が望ましい
- HTTP 層（`FavoriteController`）の MockMvc テストも同様
- `UnauthorizedAccessException` クラスは architecture.md で設計として定義されているため、Cart 管理実装時に合わせて共通実装として追加することを推奨
