package com.example.ec.domain.order

import com.example.ec.domain.customer.CustomerId
import java.util.UUID

interface OrderRepository {
    fun findById(orderId: OrderId): Order?
    fun findByCustomerId(customerId: CustomerId, page: Int, size: Int): List<Order>
    fun save(order: Order)
}
