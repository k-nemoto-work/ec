package com.example.ec.usecase.order.get

import java.util.UUID

data class OrderItemResult(
    val productId: UUID,
    val productNameSnapshot: String,
    val priceSnapshot: Long,
)
