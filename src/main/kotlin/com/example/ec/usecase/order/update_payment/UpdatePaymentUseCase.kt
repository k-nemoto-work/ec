package com.example.ec.usecase.order.update_payment

import com.example.ec.domain.exception.ResourceNotFoundException
import com.example.ec.domain.exception.UnauthorizedAccessException
import com.example.ec.domain.order.OrderId
import com.example.ec.domain.order.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UpdatePaymentUseCase(
    private val orderRepository: OrderRepository,
) {

    fun execute(command: UpdatePaymentCommand) {
        val order = orderRepository.findById(OrderId(command.orderId))
            ?: throw ResourceNotFoundException("注文", command.orderId.toString())

        if (order.customerId.value != command.customerId) {
            throw UnauthorizedAccessException("この注文へのアクセス権限がありません")
        }

        val updatedOrder = order.updatePayment()
        orderRepository.save(updatedOrder)
    }
}
