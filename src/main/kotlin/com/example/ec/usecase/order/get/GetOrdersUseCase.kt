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
        val orders = orderRepository.findByCustomerId(CustomerId(customerId), page, size)
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
            page = page,
            size = size,
        )
    }
}
