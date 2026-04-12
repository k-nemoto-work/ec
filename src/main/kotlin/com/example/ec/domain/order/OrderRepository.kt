package com.example.ec.domain.order

import com.example.ec.domain.customer.CustomerId

interface OrderRepository {
    fun findById(orderId: OrderId): Order?
    fun findByCustomerId(customerId: CustomerId, page: Int, size: Int): List<Order>
    fun countByCustomerId(customerId: CustomerId): Long
    fun save(order: Order)
}
