package com.example.ec.domain.order

enum class OrderStatus {
    PENDING,    // 未確定
    // TODO: PENDING → CONFIRMED への遷移ロジック（注文確認ユースケース）は未実装
    CONFIRMED,  // 確定
    SHIPPING,   // 配送中
    DELIVERED,  // 配送完了
    CANCELLED,  // キャンセル
}
