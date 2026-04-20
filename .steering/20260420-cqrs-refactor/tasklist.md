# タスクリスト: CQRS-Light リファクタリング

## フェーズ1: Product

- [x] `usecase/product/list/ProductQueryService.kt` インターフェース作成
- [x] `infrastructure/query/ProductQueryServiceImpl.kt` 実装（2クエリ/1トランザクション）
- [ ] `infrastructure/query/ProductQueryServiceImplTest.kt` 統合テスト作成
- [x] `ListProductsUseCase.kt` をProductQueryService使用に変更
- [x] `ListProductsUseCaseTest.kt` モック差し替え

## フェーズ2: Favorite

- [x] `usecase/favorite/get/FavoriteQueryService.kt` インターフェース作成
- [x] `infrastructure/query/FavoriteQueryServiceImpl.kt` 実装（3テーブルJOIN）
- [x] `infrastructure/query/FavoriteQueryServiceImplTest.kt` 統合テスト作成（削除商品ケース含む）
- [x] `GetFavoriteUseCase.kt` をFavoriteQueryService使用に変更
- [x] `GetFavoriteUseCaseTest.kt` モック差し替え

## フェーズ3: Order

- [x] `usecase/order/get/OrderQueryService.kt` インターフェース作成
- [x] `infrastructure/query/OrderQueryServiceImpl.kt` 実装（GROUP BY + JOIN）
- [x] `infrastructure/query/OrderQueryServiceImplTest.kt` 統合テスト作成
- [x] `GetOrdersUseCase.kt` をOrderQueryService使用に変更
- [x] `GetOrdersUseCaseTest.kt` 作成

## フェーズ4: 検証

- [x] ユニットテスト（UseCase層・Domain層）グリーン確認
- [ ] 統合テスト（TestContainers）はdevcontainerで実行確認

## 実装後の振り返り

- 実装完了日: 2026-04-20
- 計画との差分:
  - Window関数（COUNT(*) OVER()）はExposedでの確認ができないため、ProductQueryServiceImplは2クエリ/1トランザクション方式を採用（設計に記載済みのフォールバック）
  - FavoriteQueryServiceImplのleftJoin構文はExposedの `join(table, JoinType.LEFT, col1, col2)` を使用（ラムダ引数構文はExposed 0.56非対応）
- 学んだこと:
  - IDEのキャッシュ問題：同一パッケージ内の新規ファイル参照は、IDEには「未解決参照」と誤表示されることがあるが、コンパイルは正常に通る
  - Exposedの `deleteAll` は `import org.jetbrains.exposed.sql.deleteAll` が必要
  - `join()` 関数による明示的なJOIN条件指定が推奨
- 次回への改善提案:
  - devcontainerでの統合テスト実行検証
  - Window関数のExposed対応確認後、ProductQueryServiceImplをさらに最適化する余地あり
