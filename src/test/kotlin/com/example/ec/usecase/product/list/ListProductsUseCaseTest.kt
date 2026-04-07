package com.example.ec.usecase.product.list

import com.example.ec.domain.product.*
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals

class ListProductsUseCaseTest {

    private val productRepository: ProductRepository = mockk()
    private lateinit var useCase: ListProductsUseCase

    @BeforeEach
    fun setUp() {
        useCase = ListProductsUseCase(productRepository)
    }

    @Test
    fun `販売中の商品一覧を取得できる`() {
        // Given
        val products = listOf(
            createProduct("商品A"),
            createProduct("商品B"),
        )
        every { productRepository.findAllOnSale(null, 0, 20) } returns products
        every { productRepository.countOnSale(null) } returns 2L

        // When
        val result = useCase.execute(ListProductsQuery())

        // Then
        assertEquals(2, result.products.size)
        assertEquals(2L, result.totalCount)
        assertEquals(0, result.page)
        assertEquals(20, result.size)
    }

    @Test
    fun `カテゴリIDで絞り込んだ商品一覧を取得できる`() {
        // Given
        val categoryId = UUID.randomUUID()
        every { productRepository.findAllOnSale(CategoryId(categoryId), 0, 20) } returns emptyList()
        every { productRepository.countOnSale(CategoryId(categoryId)) } returns 0L

        // When
        val result = useCase.execute(ListProductsQuery(categoryId = categoryId))

        // Then
        assertEquals(0, result.products.size)
        assertEquals(0L, result.totalCount)
    }

    private fun createProduct(name: String): Product =
        Product(
            id = ProductId.generate(),
            name = ProductName(name),
            price = Money(1000),
            description = "説明",
            categoryId = CategoryId(UUID.randomUUID()),
            status = ProductStatus.ON_SALE,
        )
}
