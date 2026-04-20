# 設計: CQRS-Light リファクタリング

## アーキテクチャ

```
UseCase層    → QueryService interface (新規) → 既存DTOを直接返却
Infrastructure層 → QueryServiceImpl (新規) → 最適化JOINクエリ
Domain層     → 変更なし (ドメインリポジトリは書き込み専用に)
```

## 新規パッケージ

- QueryService interface: `usecase/{aggregate}/get/` または `usecase/{aggregate}/list/` に配置
- QueryService impl: `infrastructure/query/` (新パッケージ)

## SQL最適化

### ProductQueryServiceImpl
```sql
SELECT id, name, price, category_id, status, COUNT(*) OVER () AS total_count
FROM products WHERE status = 'ON_SALE' [AND category_id = ?]
ORDER BY created_at DESC LIMIT ? OFFSET ?
```

### OrderQueryServiceImpl
```sql
SELECT o.id, o.total_amount, o.status, o.ordered_at,
       COUNT(oi.product_id) AS item_count
FROM orders o LEFT JOIN order_items oi ON oi.order_id = o.id
WHERE o.customer_id = ?
GROUP BY o.id, o.total_amount, o.status, o.ordered_at
ORDER BY o.ordered_at DESC LIMIT ? OFFSET ?
-- カウントは別途 COUNT(DISTINCT o.id) クエリ（Window関数+GROUPBYのExposed対応を考慮）
```

### FavoriteQueryServiceImpl
```sql
SELECT f.id, fi.product_id, fi.added_at, p.name, p.price, p.status
FROM favorites f
LEFT JOIN favorite_items fi ON fi.favorite_id = f.id
LEFT JOIN products p ON p.id = fi.product_id
WHERE f.customer_id = ?
ORDER BY fi.added_at ASC
```

## テスト戦略

- UseCase層: モックテスト (QueryServiceをモック)
- Infrastructure層: TestContainersによる統合テスト
