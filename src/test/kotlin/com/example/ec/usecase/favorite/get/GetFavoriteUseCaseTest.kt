package com.example.ec.usecase.favorite.get

import com.example.ec.domain.customer.CustomerId
import com.example.ec.domain.customer.Favorite
import com.example.ec.domain.customer.FavoriteId
import com.example.ec.domain.customer.FavoriteItem
import com.example.ec.domain.customer.FavoriteRepository
import com.example.ec.domain.product.*
import io.mockk.every
import io.mockk.mockk
import kotlinx.datetime.Clock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetFavoriteUseCaseTest {

    private val favoriteRepository: FavoriteRepository = mockk()
    private val productRepository: ProductRepository = mockk()
    private lateinit var useCase: GetFavoriteUseCase

    @BeforeEach
    fun setUp() {
        useCase = GetFavoriteUseCase(favoriteRepository, productRepository)
    }

    @Test
    fun `お気に入りが存在する場合、商品情報付きで返す`() {
        // Given
        val customerId = UUID.randomUUID()
        val productId = UUID.randomUUID()
        val categoryId = UUID.randomUUID()

        val favorite = Favorite(
            id = FavoriteId(UUID.randomUUID()),
            customerId = CustomerId(customerId),
            items = listOf(FavoriteItem(productId = ProductId(productId), addedAt = Clock.System.now())),
        )
        val product = Product(
            id = ProductId(productId),
            name = ProductName("お気に入り商品"),
            price = Money(3000),
            description = "説明",
            categoryId = CategoryId(categoryId),
            status = ProductStatus.ON_SALE,
        )

        every { favoriteRepository.findByCustomerId(CustomerId(customerId)) } returns favorite
        every { productRepository.findAllByIds(listOf(ProductId(productId))) } returns listOf(product)

        // When
        val result = useCase.execute(customerId)

        // Then
        assertEquals(1, result.items.size)
        assertEquals(productId, result.items[0].productId)
        assertEquals("お気に入り商品", result.items[0].productName)
        assertEquals(3000L, result.items[0].price)
    }

    @Test
    fun `お気に入りが存在しない場合、空リストを返す`() {
        // Given
        val customerId = UUID.randomUUID()
        every { favoriteRepository.findByCustomerId(CustomerId(customerId)) } returns null

        // When
        val result = useCase.execute(customerId)

        // Then
        assertTrue(result.items.isEmpty())
    }
}
