package com.example.ec.usecase.product.update

import com.example.ec.domain.exception.BusinessRuleViolationException
import com.example.ec.domain.exception.ResourceNotFoundException
import com.example.ec.domain.product.*
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID
import kotlin.test.assertEquals

class UpdateProductUseCaseTest {

    private val productRepository: ProductRepository = mockk()
    private val categoryRepository: CategoryRepository = mockk()
    private lateinit var useCase: UpdateProductUseCase

    @BeforeEach
    fun setUp() {
        useCase = UpdateProductUseCase(productRepository, categoryRepository)
    }

    @Test
    fun `正常に商品を更新できる`() {
        // Given
        val productId = UUID.randomUUID()
        val categoryId = UUID.randomUUID()
        val product = Product(
            id = ProductId(productId),
            name = ProductName("旧名前"),
            price = Money(1000),
            description = "旧説明",
            categoryId = CategoryId(categoryId),
            status = ProductStatus.ON_SALE,
        )
        val command = UpdateProductCommand(
            productId = productId,
            name = "新名前",
            price = 2000L,
            description = "新説明",
            categoryId = categoryId,
        )
        every { productRepository.findById(ProductId(productId)) } returns product
        every { categoryRepository.findById(CategoryId(categoryId)) } returns Category(CategoryId(categoryId), "古着")
        every { productRepository.update(any()) } just Runs

        // When
        val result = useCase.execute(command)

        // Then
        assertEquals("新名前", result.name)
        assertEquals(2000L, result.price)
        verify { productRepository.update(any()) }
    }

    @Test
    fun `存在しない商品ではResourceNotFoundExceptionが発生する`() {
        // Given
        val productId = UUID.randomUUID()
        val command = UpdateProductCommand(
            productId = productId,
            name = "名前",
            price = 1000L,
            description = "",
            categoryId = UUID.randomUUID(),
        )
        every { productRepository.findById(ProductId(productId)) } returns null

        // When / Then
        assertThrows<ResourceNotFoundException> { useCase.execute(command) }
    }

    @Test
    fun `RESERVED状態の商品更新はBusinessRuleViolationExceptionが発生する`() {
        // Given
        val productId = UUID.randomUUID()
        val categoryId = UUID.randomUUID()
        val product = Product(
            id = ProductId(productId),
            name = ProductName("商品"),
            price = Money(1000),
            description = "",
            categoryId = CategoryId(categoryId),
            status = ProductStatus.RESERVED,
        )
        val command = UpdateProductCommand(
            productId = productId,
            name = "新名前",
            price = 2000L,
            description = "",
            categoryId = categoryId,
        )
        every { productRepository.findById(ProductId(productId)) } returns product
        every { categoryRepository.findById(CategoryId(categoryId)) } returns Category(CategoryId(categoryId), "古着")

        // When / Then
        assertThrows<BusinessRuleViolationException> { useCase.execute(command) }
    }
}
