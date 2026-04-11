package com.example.ec.usecase.order.get

data class OrdersResult(
    val orders: List<OrderSummaryResult>,
    val page: Int,
    val size: Int,
)
