package com.example.ec.usecase.order.update_shipment

import com.example.ec.domain.exception.ResourceNotFoundException
import com.example.ec.domain.exception.UnauthorizedAccessException
import com.example.ec.domain.order.OrderId
import com.example.ec.domain.order.OrderRepository
import com.example.ec.domain.order.ShipmentStatus
import com.example.ec.domain.product.ProductRepository
import com.example.ec.domain.product.ProductStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UpdateShipmentUseCase(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
) {

    fun execute(command: UpdateShipmentCommand) {
        val order = orderRepository.findById(OrderId(command.orderId))
            ?: throw ResourceNotFoundException("注文", command.orderId.toString())

        if (order.customerId.value != command.customerId) {
            throw UnauthorizedAccessException("この注文へのアクセス権限がありません")
        }

        val updatedOrder = order.updateShipment(command.newStatus)
        orderRepository.save(updatedOrder)

        if (command.newStatus == ShipmentStatus.DELIVERED) {
            val products = productRepository.findAllByIds(order.items.map { it.productId })
            val soldProducts = products.map { it.changeStatus(ProductStatus.SOLD) }
            soldProducts.forEach { productRepository.update(it) }
        }
    }
}
