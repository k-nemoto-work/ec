package com.example.ec.usecase.cart.add

import com.example.ec.domain.customer.CustomerId
import com.example.ec.domain.exception.BusinessRuleViolationException
import com.example.ec.domain.exception.ResourceNotFoundException
import com.example.ec.domain.order.Cart
import com.example.ec.domain.order.CartId
import com.example.ec.domain.order.CartItem
import com.example.ec.domain.order.CartRepository
import com.example.ec.domain.product.*
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.datetime.Clock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class AddToCartUseCaseTest {

    private val cartRepository: CartRepository = mockk()
    private val productRepository: ProductRepository = mockk()
    private lateinit var useCase: AddToCartUseCase

    @BeforeEach
    fun setUp() {
        useCase = AddToCartUseCase(cartRepository, productRepository)
    }

    @Test
    fun `カートが未作成の場合、新規作成して商品を追加する`() {
        // Given
        val customerId = UUID.randomUUID()
        val productId = UUID.randomUUID()
        val product = createProduct(productId, ProductStatus.ON_SALE)

        every { productRepository.findById(ProductId(productId)) } returns product
        every { cartRepository.findByCustomerId(CustomerId(customerId)) } returns null
        justRun { cartRepository.save(any()) }

        // When
        useCase.execute(AddToCartCommand(customerId = customerId, productId = productId))

        // Then
        verify(exactly = 1) { cartRepository.save(any()) }
    }

    @Test
    fun `カートが既存の場合、既存に商品を追加する`() {
        // Given
        val customerId = UUID.randomUUID()
        val productId = UUID.randomUUID()
        val product = createProduct(productId, ProductStatus.ON_SALE)
        val existingCart = Cart(
            id = CartId(UUID.randomUUID()),
            customerId = CustomerId(customerId),
            items = emptyList(),
        )

        every { productRepository.findById(ProductId(productId)) } returns product
        every { cartRepository.findByCustomerId(CustomerId(customerId)) } returns existingCart
        justRun { cartRepository.save(any()) }

        // When
        useCase.execute(AddToCartCommand(customerId = customerId, productId = productId))

        // Then
        verify(exactly = 1) { cartRepository.save(any()) }
    }

    @Test
    fun `存在しない商品を追加するとResourceNotFoundExceptionが発生する`() {
        // Given
        val customerId = UUID.randomUUID()
        val productId = UUID.randomUUID()

        every { productRepository.findById(ProductId(productId)) } returns null

        // When / Then
        assertThrows<ResourceNotFoundException> {
            useCase.execute(AddToCartCommand(customerId = customerId, productId = productId))
        }
    }

    @Test
    fun `ON_SALE以外の商品を追加するとBusinessRuleViolationExceptionが発生する`() {
        // Given
        val customerId = UUID.randomUUID()
        val productId = UUID.randomUUID()
        val product = createProduct(productId, ProductStatus.RESERVED)

        every { productRepository.findById(ProductId(productId)) } returns product
        every { cartRepository.findByCustomerId(CustomerId(customerId)) } returns null

        // When / Then
        assertThrows<BusinessRuleViolationException> {
            useCase.execute(AddToCartCommand(customerId = customerId, productId = productId))
        }
    }

    @Test
    fun `既にカートにある商品を追加するとBusinessRuleViolationExceptionが発生する`() {
        // Given
        val customerId = UUID.randomUUID()
        val productId = UUID.randomUUID()
        val product = createProduct(productId, ProductStatus.ON_SALE)
        val existingCart = Cart(
            id = CartId(UUID.randomUUID()),
            customerId = CustomerId(customerId),
            items = listOf(CartItem(productId = ProductId(productId), addedAt = Clock.System.now())),
        )

        every { productRepository.findById(ProductId(productId)) } returns product
        every { cartRepository.findByCustomerId(CustomerId(customerId)) } returns existingCart

        // When / Then
        assertThrows<BusinessRuleViolationException> {
            useCase.execute(AddToCartCommand(customerId = customerId, productId = productId))
        }
    }

    private fun createProduct(productId: UUID, status: ProductStatus): Product =
        Product(
            id = ProductId(productId),
            name = ProductName("テスト商品"),
            price = Money(1000),
            description = "説明",
            categoryId = CategoryId(UUID.randomUUID()),
            status = status,
        )
}
