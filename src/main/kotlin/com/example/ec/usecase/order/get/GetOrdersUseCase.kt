package com.example.ec.usecase.order.get

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class GetOrdersUseCase(
    private val orderQueryService: OrderQueryService,
) {

    fun execute(customerId: UUID, page: Int, size: Int): OrdersResult =
        orderQueryService.findOrderSummariesByCustomer(customerId, page, size)
}
