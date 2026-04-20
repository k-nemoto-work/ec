package com.example.ec.usecase.favorite.get

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetFavoriteUseCaseTest {

    private val favoriteQueryService: FavoriteQueryService = mockk()
    private lateinit var useCase: GetFavoriteUseCase

    @BeforeEach
    fun setUp() {
        useCase = GetFavoriteUseCase(favoriteQueryService)
    }

    @Test
    fun `お気に入りが存在する場合、商品情報付きで返す`() {
        // Given
        val customerId = UUID.randomUUID()
        val productId = UUID.randomUUID()
        val favoriteId = UUID.randomUUID()
        val result = FavoriteResult(
            favoriteId = favoriteId,
            items = listOf(
                FavoriteItemResult(
                    productId = productId,
                    productName = "お気に入り商品",
                    price = 3000L,
                    status = "ON_SALE",
                    addedAt = "2026-01-01T00:00:00Z",
                ),
            ),
        )
        every { favoriteQueryService.findByCustomerId(customerId) } returns result

        // When
        val actual = useCase.execute(customerId)

        // Then
        assertEquals(1, actual.items.size)
        assertEquals(productId, actual.items[0].productId)
        assertEquals("お気に入り商品", actual.items[0].productName)
        assertEquals(3000L, actual.items[0].price)
    }

    @Test
    fun `お気に入りが存在しない場合、空リストを返す`() {
        // Given
        val customerId = UUID.randomUUID()
        every { favoriteQueryService.findByCustomerId(customerId) } returns FavoriteResult(
            favoriteId = null,
            items = emptyList(),
        )

        // When
        val actual = useCase.execute(customerId)

        // Then
        assertTrue(actual.items.isEmpty())
    }

    @Test
    fun `カタログから削除された商品はレスポンスに含まれない`() {
        // Given
        val customerId = UUID.randomUUID()
        val existingProductId = UUID.randomUUID()
        every { favoriteQueryService.findByCustomerId(customerId) } returns FavoriteResult(
            favoriteId = UUID.randomUUID(),
            items = listOf(
                FavoriteItemResult(
                    productId = existingProductId,
                    productName = "存在する商品",
                    price = 1000L,
                    status = "ON_SALE",
                    addedAt = "2026-01-01T00:00:00Z",
                ),
            ),
        )

        // When
        val actual = useCase.execute(customerId)

        // Then
        assertEquals(1, actual.items.size)
        assertEquals(existingProductId, actual.items[0].productId)
    }
}
