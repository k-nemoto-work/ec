package com.example.ec.usecase.order.get

import java.util.UUID

data class OrderResult(
    val orderId: UUID,
    val customerId: UUID,
    val items: List<OrderItemResult>,
    val totalAmount: Long,
    val status: String,
    val paymentMethod: String,
    val paymentStatus: String,
    val shipmentStatus: String,
    val shippingPostalCode: String,
    val shippingPrefecture: String,
    val shippingCity: String,
    val shippingStreetAddress: String,
    val orderedAt: String,
)
