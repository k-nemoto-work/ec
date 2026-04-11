package com.example.ec.usecase.order.place

import com.example.ec.domain.customer.CustomerId
import com.example.ec.domain.exception.BusinessRuleViolationException
import com.example.ec.domain.order.CartRepository
import com.example.ec.domain.order.Order
import com.example.ec.domain.order.OrderRepository
import com.example.ec.domain.product.ProductRepository
import com.example.ec.domain.product.ProductStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class PlaceOrderUseCase(
    private val cartRepository: CartRepository,
    private val productRepository: ProductRepository,
    private val orderRepository: OrderRepository,
) {

    fun execute(command: PlaceOrderCommand): PlaceOrderResult {
        val customerId = CustomerId(command.customerId)

        val cart = cartRepository.findByCustomerId(customerId)
            ?: throw BusinessRuleViolationException("カートが存在しません")

        if (cart.items.isEmpty()) {
            throw BusinessRuleViolationException("カートに商品がありません。注文するには商品をカートに追加してください")
        }

        val productIds = cart.items.map { it.productId }
        val products = productRepository.findAllByIds(productIds)

        if (products.size != productIds.size) {
            throw BusinessRuleViolationException("カート内の一部の商品が見つかりません")
        }

        products.forEach { product ->
            if (product.status != ProductStatus.ON_SALE) {
                throw BusinessRuleViolationException(
                    "カート内に注文できない商品が含まれています: ${product.name.value}（ステータス: ${product.status}）"
                )
            }
        }

        val order = Order.create(
            customerId = customerId,
            products = products,
            shippingAddress = command.shippingAddress,
            paymentMethod = command.paymentMethod,
        )

        val updatedProducts = products.map { it.changeStatus(ProductStatus.RESERVED) }

        orderRepository.save(order)
        updatedProducts.forEach { productRepository.update(it) }

        val clearedCart = cart.copy(items = emptyList())
        cartRepository.save(clearedCart)

        return PlaceOrderResult(orderId = order.id.value)
    }
}
