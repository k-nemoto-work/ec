# 設計

## 修正方針
- 横断的な矛盾は正となるドキュメントを決め、他を合わせる
- Money型: `Long` が正（glossary.md, functional-design.md）→ development-guidelines.mdを修正
- PaymentStatus: functional-design.mdの2値（UNPAID/PAID）が正 → glossary.mdにスコープ外注記
- テストパッケージ: `com/example/ec/` が正 → architecture.mdを修正
- CartItem: functional-design.mdの`productId`のみが正 → glossary.mdを修正
