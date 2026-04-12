package com.example.ec.usecase.order.get

import com.example.ec.domain.customer.CustomerId
import com.example.ec.domain.order.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class GetOrdersUseCase(
    private val orderRepository: OrderRepository,
) {

    fun execute(customerId: UUID, page: Int, size: Int): OrdersResult {
        val id = CustomerId(customerId)
        val orders = orderRepository.findByCustomerId(id, page, size)
        val totalCount = orderRepository.countByCustomerId(id)
        return OrdersResult(
            orders = orders.map { order ->
                OrderSummaryResult(
                    orderId = order.id.value,
                    totalAmount = order.totalAmount.amount,
                    status = order.status.name,
                    itemCount = order.items.size,
                    orderedAt = order.orderedAt.toString(),
                )
            },
            totalCount = totalCount,
            page = page,
            size = size,
        )
    }
}
