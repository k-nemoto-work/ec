package com.example.ec.domain.order

enum class OrderStatus {
    CONFIRMED,  // 確定
    SHIPPING,   // 配送中
    DELIVERED,  // 配送完了
    CANCELLED,  // キャンセル
}
