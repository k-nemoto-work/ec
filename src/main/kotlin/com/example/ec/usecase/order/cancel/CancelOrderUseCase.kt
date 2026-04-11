package com.example.ec.usecase.order.cancel

import com.example.ec.domain.exception.ResourceNotFoundException
import com.example.ec.domain.exception.UnauthorizedAccessException
import com.example.ec.domain.order.OrderId
import com.example.ec.domain.order.OrderRepository
import com.example.ec.domain.product.ProductRepository
import com.example.ec.domain.product.ProductStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class CancelOrderUseCase(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
) {

    fun execute(orderId: UUID, customerId: UUID) {
        val order = orderRepository.findById(OrderId(orderId))
            ?: throw ResourceNotFoundException("注文", orderId.toString())

        if (order.customerId.value != customerId) {
            throw UnauthorizedAccessException("この注文へのアクセス権限がありません")
        }

        val cancelledOrder = order.cancel()

        val products = productRepository.findAllByIds(order.items.map { it.productId })
        val restoredProducts = products.map { it.changeStatus(ProductStatus.ON_SALE) }

        orderRepository.save(cancelledOrder)
        restoredProducts.forEach { productRepository.update(it) }
    }
}
