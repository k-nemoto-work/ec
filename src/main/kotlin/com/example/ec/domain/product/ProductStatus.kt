package com.example.ec.domain.product

enum class ProductStatus {
    ON_SALE,    // 販売中
    RESERVED,   // 売約済み（注文確定後）
    SOLD,       // 売却済み（配送完了後）
    PRIVATE,    // 非公開
}
