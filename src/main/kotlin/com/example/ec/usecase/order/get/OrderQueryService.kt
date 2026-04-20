package com.example.ec.usecase.order.get

import java.util.UUID

interface OrderQueryService {
    fun findOrderSummariesByCustomer(customerId: UUID, page: Int, size: Int): OrdersResult
}
