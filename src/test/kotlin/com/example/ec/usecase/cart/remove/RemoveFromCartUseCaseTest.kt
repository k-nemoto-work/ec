package com.example.ec.usecase.cart.remove

import com.example.ec.domain.customer.CustomerId
import com.example.ec.domain.exception.ResourceNotFoundException
import com.example.ec.domain.order.Cart
import com.example.ec.domain.order.CartId
import com.example.ec.domain.order.CartItem
import com.example.ec.domain.order.CartRepository
import com.example.ec.domain.product.ProductId
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class RemoveFromCartUseCaseTest {

    private val cartRepository: CartRepository = mockk()
    private lateinit var useCase: RemoveFromCartUseCase

    @BeforeEach
    fun setUp() {
        useCase = RemoveFromCartUseCase(cartRepository)
    }

    @Test
    fun `カートから商品を削除できる`() {
        // Given
        val customerId = UUID.randomUUID()
        val productId = UUID.randomUUID()
        val cart = Cart(
            id = CartId(UUID.randomUUID()),
            customerId = CustomerId(customerId),
            items = listOf(CartItem(productId = ProductId(productId))),
        )

        every { cartRepository.findByCustomerId(CustomerId(customerId)) } returns cart
        justRun { cartRepository.save(any()) }

        // When
        useCase.execute(RemoveFromCartCommand(customerId = customerId, productId = productId))

        // Then
        verify(exactly = 1) { cartRepository.save(any()) }
    }

    @Test
    fun `カートが存在しない場合、ResourceNotFoundExceptionが発生する`() {
        // Given
        val customerId = UUID.randomUUID()
        val productId = UUID.randomUUID()

        every { cartRepository.findByCustomerId(CustomerId(customerId)) } returns null

        // When / Then
        assertThrows<ResourceNotFoundException> {
            useCase.execute(RemoveFromCartCommand(customerId = customerId, productId = productId))
        }
    }

    @Test
    fun `カートに存在しない商品を削除するとResourceNotFoundExceptionが発生する`() {
        // Given
        val customerId = UUID.randomUUID()
        val productId = UUID.randomUUID()
        val anotherProductId = UUID.randomUUID()
        val cart = Cart(
            id = CartId(UUID.randomUUID()),
            customerId = CustomerId(customerId),
            items = listOf(CartItem(productId = ProductId(anotherProductId))),
        )

        every { cartRepository.findByCustomerId(CustomerId(customerId)) } returns cart

        // When / Then
        assertThrows<ResourceNotFoundException> {
            useCase.execute(RemoveFromCartCommand(customerId = customerId, productId = productId))
        }
    }
}
