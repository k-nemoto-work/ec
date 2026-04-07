package com.example.ec.usecase.product.get

import com.example.ec.domain.exception.ResourceNotFoundException
import com.example.ec.domain.product.*
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID
import kotlin.test.assertEquals

class GetProductUseCaseTest {

    private val productRepository: ProductRepository = mockk()
    private lateinit var useCase: GetProductUseCase

    @BeforeEach
    fun setUp() {
        useCase = GetProductUseCase(productRepository)
    }

    @Test
    fun `販売中の商品詳細を取得できる`() {
        // Given
        val productId = UUID.randomUUID()
        val product = Product(
            id = ProductId(productId),
            name = ProductName("テスト商品"),
            price = Money(1500),
            description = "説明",
            categoryId = CategoryId(UUID.randomUUID()),
            status = ProductStatus.ON_SALE,
        )
        every { productRepository.findById(ProductId(productId)) } returns product

        // When
        val result = useCase.execute(productId)

        // Then
        assertEquals(productId, result.productId)
        assertEquals("テスト商品", result.name)
        assertEquals(1500L, result.price)
    }

    @Test
    fun `存在しない商品IDではResourceNotFoundExceptionが発生する`() {
        // Given
        val productId = UUID.randomUUID()
        every { productRepository.findById(ProductId(productId)) } returns null

        // When / Then
        assertThrows<ResourceNotFoundException> { useCase.execute(productId) }
    }

    @Test
    fun `販売中でない商品はResourceNotFoundExceptionが発生する`() {
        // Given
        val productId = UUID.randomUUID()
        val product = Product(
            id = ProductId(productId),
            name = ProductName("非公開商品"),
            price = Money(1000),
            description = "",
            categoryId = CategoryId(UUID.randomUUID()),
            status = ProductStatus.PRIVATE,
        )
        every { productRepository.findById(ProductId(productId)) } returns product

        // When / Then
        assertThrows<ResourceNotFoundException> { useCase.execute(productId) }
    }
}
