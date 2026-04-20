package com.example.ec.usecase.product.list

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals

class ListProductsUseCaseTest {

    private val productQueryService: ProductQueryService = mockk()
    private lateinit var useCase: ListProductsUseCase

    @BeforeEach
    fun setUp() {
        useCase = ListProductsUseCase(productQueryService)
    }

    @Test
    fun `販売中の商品一覧を取得できる`() {
        // Given
        val result = ProductListResult(
            products = listOf(
                ProductSummary(UUID.randomUUID(), "商品A", 1000L, UUID.randomUUID(), "ON_SALE"),
                ProductSummary(UUID.randomUUID(), "商品B", 2000L, UUID.randomUUID(), "ON_SALE"),
            ),
            totalCount = 2L,
            page = 0,
            size = 20,
        )
        every { productQueryService.findPageWithCount(ListProductsQuery()) } returns result

        // When
        val actual = useCase.execute(ListProductsQuery())

        // Then
        assertEquals(2, actual.products.size)
        assertEquals(2L, actual.totalCount)
        assertEquals(0, actual.page)
        assertEquals(20, actual.size)
    }

    @Test
    fun `カテゴリIDで絞り込んだ商品一覧を取得できる`() {
        // Given
        val categoryId = UUID.randomUUID()
        val query = ListProductsQuery(categoryId = categoryId)
        val result = ProductListResult(products = emptyList(), totalCount = 0L, page = 0, size = 20)
        every { productQueryService.findPageWithCount(query) } returns result

        // When
        val actual = useCase.execute(query)

        // Then
        assertEquals(0, actual.products.size)
        assertEquals(0L, actual.totalCount)
    }
}
