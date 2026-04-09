# タスクリスト: レビュー指摘修正

## フェーズ1: リポジトリ設計修正

- [x] FavoriteRepository.kt から `update` を削除し `save` のみに統合
- [x] FavoriteRepositoryImpl.kt を差分更新 + ORDER BY に書き換え
- [x] AddToFavoriteUseCase.kt の isNew 分岐を削除し `save` のみ呼ぶ
- [x] RemoveFromFavoriteUseCase.kt の `update` 呼び出しを `save` に変更

## フェーズ2: テスト・ドキュメント修正

- [x] GetFavoriteUseCaseTest.kt に削除済み商品のテストを追加
- [x] V6__create_favorites.sql に FK なしの意図コメントを追加
- [x] ~~FavoriteController.kt に @Valid を追加~~（スキップ: `spring-boot-starter-validation` が未依存、既存の CustomerController も同パターンのため見送り）

## フェーズ3: ビルド・テスト確認

- [x] `./gradlew test` を実行してテストがパスすること確認

---

## 実装後の振り返り

### 実装完了日
2026-04-09

### 計画と実績の差分

- `@Valid` 追加は `spring-boot-starter-validation` が未依存のためスキップ。既存の `CustomerController` も同パターンのため、プロジェクトとして依存追加が必要な別タスク。
- `AddToFavoriteUseCaseTest` / `RemoveFromFavoriteUseCaseTest` の mock も `update` → `save` への変更が必要だったため追加修正。

### 学んだこと

- `save/update` 分離はユースケース層に永続化の詳細を漏らす典型的な設計ミス。リポジトリは「存在チェック→条件分岐INSERT」で吸収できる。
- delete-all + re-insert は実装が単純だが、`added_at` の保持や並行安全性を考えると差分更新の方が堅牢。
