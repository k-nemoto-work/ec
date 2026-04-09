package com.example.ec.usecase.cart.get

import com.example.ec.domain.customer.CustomerId
import com.example.ec.domain.order.CartRepository
import com.example.ec.domain.product.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class GetCartUseCase(
    private val cartRepository: CartRepository,
    private val productRepository: ProductRepository,
) {

    fun execute(customerId: UUID): CartResult {
        val cart = cartRepository.findByCustomerId(CustomerId(customerId))
            ?: return CartResult(cartId = null, items = emptyList(), totalAmount = 0L)

        val productIds = cart.items.map { it.productId }
        val products = productRepository.findAllByIds(productIds).associateBy { it.id }

        val itemResults = cart.items.mapNotNull { item ->
            val product = products[item.productId] ?: return@mapNotNull null
            CartItemResult(
                productId = item.productId.value,
                productName = product.name.value,
                price = product.price.amount,
                status = product.status.name,
            )
        }

        val totalAmount = itemResults.sumOf { it.price }

        return CartResult(
            cartId = cart.id.value,
            items = itemResults,
            totalAmount = totalAmount,
        )
    }
}
