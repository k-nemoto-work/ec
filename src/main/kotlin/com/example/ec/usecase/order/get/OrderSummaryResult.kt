package com.example.ec.usecase.order.get

import java.util.UUID

data class OrderSummaryResult(
    val orderId: UUID,
    val totalAmount: Long,
    val status: String,
    val itemCount: Int,
    val orderedAt: String,
)
