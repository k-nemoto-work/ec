package com.example.ec.usecase.favorite.remove

import com.example.ec.domain.customer.CustomerId
import com.example.ec.domain.customer.Favorite
import com.example.ec.domain.customer.FavoriteId
import com.example.ec.domain.customer.FavoriteItem
import com.example.ec.domain.customer.FavoriteRepository
import com.example.ec.domain.exception.ResourceNotFoundException
import com.example.ec.domain.product.ProductId
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.datetime.Clock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class RemoveFromFavoriteUseCaseTest {

    private val favoriteRepository: FavoriteRepository = mockk()
    private lateinit var useCase: RemoveFromFavoriteUseCase

    @BeforeEach
    fun setUp() {
        useCase = RemoveFromFavoriteUseCase(favoriteRepository)
    }

    @Test
    fun `お気に入りから商品を削除できる`() {
        // Given
        val customerId = UUID.randomUUID()
        val productId = UUID.randomUUID()
        val favorite = Favorite(
            id = FavoriteId(UUID.randomUUID()),
            customerId = CustomerId(customerId),
            items = listOf(FavoriteItem(productId = ProductId(productId), addedAt = Clock.System.now())),
        )

        every { favoriteRepository.findByCustomerId(CustomerId(customerId)) } returns favorite
        justRun { favoriteRepository.update(any()) }

        // When
        useCase.execute(RemoveFromFavoriteCommand(customerId = customerId, productId = productId))

        // Then
        verify(exactly = 1) { favoriteRepository.update(any()) }
    }

    @Test
    fun `お気に入りが存在しない場合ResourceNotFoundExceptionが発生する`() {
        // Given
        val customerId = UUID.randomUUID()
        val productId = UUID.randomUUID()

        every { favoriteRepository.findByCustomerId(CustomerId(customerId)) } returns null

        // When / Then
        assertThrows<ResourceNotFoundException> {
            useCase.execute(RemoveFromFavoriteCommand(customerId = customerId, productId = productId))
        }
    }

    @Test
    fun `お気に入りにない商品を削除するとResourceNotFoundExceptionが発生する`() {
        // Given
        val customerId = UUID.randomUUID()
        val productId = UUID.randomUUID()
        val favorite = Favorite(
            id = FavoriteId(UUID.randomUUID()),
            customerId = CustomerId(customerId),
            items = emptyList(),
        )

        every { favoriteRepository.findByCustomerId(CustomerId(customerId)) } returns favorite

        // When / Then
        assertThrows<ResourceNotFoundException> {
            useCase.execute(RemoveFromFavoriteCommand(customerId = customerId, productId = productId))
        }
    }
}
