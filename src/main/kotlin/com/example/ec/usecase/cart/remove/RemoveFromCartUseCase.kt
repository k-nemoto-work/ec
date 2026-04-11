package com.example.ec.usecase.cart.remove

import com.example.ec.domain.customer.CustomerId
import com.example.ec.domain.exception.ResourceNotFoundException
import com.example.ec.domain.order.CartRepository
import com.example.ec.domain.product.ProductId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class RemoveFromCartUseCase(
    private val cartRepository: CartRepository,
) {

    fun execute(command: RemoveFromCartCommand) {
        val customerId = CustomerId(command.customerId)
        val productId = ProductId(command.productId)

        val cart = cartRepository.findByCustomerId(customerId)
            ?: throw ResourceNotFoundException("カート", customerId.value.toString())

        val updatedCart = cart.removeItem(productId)
        cartRepository.save(updatedCart)
    }
}
