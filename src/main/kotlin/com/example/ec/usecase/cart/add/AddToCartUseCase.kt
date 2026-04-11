package com.example.ec.usecase.cart.add

import com.example.ec.domain.customer.CustomerId
import com.example.ec.domain.exception.ResourceNotFoundException
import com.example.ec.domain.order.Cart
import com.example.ec.domain.order.CartRepository
import com.example.ec.domain.product.ProductId
import com.example.ec.domain.product.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class AddToCartUseCase(
    private val cartRepository: CartRepository,
    private val productRepository: ProductRepository,
) {

    fun execute(command: AddToCartCommand) {
        val customerId = CustomerId(command.customerId)
        val productId = ProductId(command.productId)

        val product = productRepository.findById(productId)
            ?: throw ResourceNotFoundException("商品", command.productId.toString())

        val cart = cartRepository.findByCustomerId(customerId) ?: Cart.create(customerId)
        val updatedCart = cart.addItem(product)
        cartRepository.save(updatedCart)
    }
}
