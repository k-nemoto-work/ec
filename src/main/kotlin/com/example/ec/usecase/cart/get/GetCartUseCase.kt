package com.example.ec.usecase.cart.get

import com.example.ec.domain.customer.CustomerId
import com.example.ec.domain.order.CartRepository
import com.example.ec.domain.product.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class GetCartUseCase(
    private val cartRepository: CartRepository,
    private val productRepository: ProductRepository,
) {

    fun execute(customerId: UUID): CartResult {
        val cart = cartRepository.findByCustomerId(CustomerId(customerId))
            ?: return CartResult(items = emptyList(), totalAmount = 0L)

        val productIds = cart.items.map { it.productId }
        val products = productRepository.findAllByIds(productIds).associateBy { it.id }

        val availableItems = cart.items.filter { products.containsKey(it.productId) }

        // カタログから削除された商品をカートからも除去する
        if (availableItems.size < cart.items.size) {
            cartRepository.save(cart.copy(items = availableItems))
        }

        val itemResults = availableItems.map { item ->
            val product = products.getValue(item.productId)
            CartItemResult(
                productId = item.productId.value,
                productName = product.name.value,
                price = product.price.amount,
                status = product.status.name,
            )
        }

        val totalAmount = itemResults.sumOf { it.price }

        return CartResult(
            items = itemResults,
            totalAmount = totalAmount,
        )
    }
}
