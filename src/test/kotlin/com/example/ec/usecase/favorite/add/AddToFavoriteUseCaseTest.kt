package com.example.ec.usecase.favorite.add

import com.example.ec.domain.customer.CustomerId
import com.example.ec.domain.customer.Favorite
import com.example.ec.domain.customer.FavoriteId
import com.example.ec.domain.customer.FavoriteItem
import com.example.ec.domain.customer.FavoriteRepository
import com.example.ec.domain.exception.BusinessRuleViolationException
import com.example.ec.domain.exception.ResourceNotFoundException
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

class AddToFavoriteUseCaseTest {

    private val favoriteRepository: FavoriteRepository = mockk()
    private val productRepository: ProductRepository = mockk()
    private lateinit var useCase: AddToFavoriteUseCase

    @BeforeEach
    fun setUp() {
        useCase = AddToFavoriteUseCase(favoriteRepository, productRepository)
    }

    @Test
    fun `お気に入りが未作成の場合、新規作成して商品を追加する`() {
        // Given
        val customerId = UUID.randomUUID()
        val productId = UUID.randomUUID()
        val product = createProduct(productId, ProductStatus.ON_SALE)

        every { productRepository.findById(ProductId(productId)) } returns product
        every { favoriteRepository.findByCustomerId(CustomerId(customerId)) } returns null
        justRun { favoriteRepository.save(any()) }

        // When
        useCase.execute(AddToFavoriteCommand(customerId = customerId, productId = productId))

        // Then
        verify(exactly = 1) { favoriteRepository.save(any()) }
    }

    @Test
    fun `お気に入りが既存の場合、既存に商品を追加する`() {
        // Given
        val customerId = UUID.randomUUID()
        val productId = UUID.randomUUID()
        val product = createProduct(productId, ProductStatus.ON_SALE)
        val existingFavorite = Favorite(
            id = FavoriteId(UUID.randomUUID()),
            customerId = CustomerId(customerId),
            items = emptyList(),
        )

        every { productRepository.findById(ProductId(productId)) } returns product
        every { favoriteRepository.findByCustomerId(CustomerId(customerId)) } returns existingFavorite
        justRun { favoriteRepository.save(any()) }

        // When
        useCase.execute(AddToFavoriteCommand(customerId = customerId, productId = productId))

        // Then
        verify(exactly = 1) { favoriteRepository.save(any()) }
    }

    @Test
    fun `存在しない商品を追加するとResourceNotFoundExceptionが発生する`() {
        // Given
        val customerId = UUID.randomUUID()
        val productId = UUID.randomUUID()

        every { productRepository.findById(ProductId(productId)) } returns null

        // When / Then
        assertThrows<ResourceNotFoundException> {
            useCase.execute(AddToFavoriteCommand(customerId = customerId, productId = productId))
        }
    }

    @Test
    fun `SOLDの商品を追加するとBusinessRuleViolationExceptionが発生する`() {
        // Given
        val customerId = UUID.randomUUID()
        val productId = UUID.randomUUID()
        val product = createProduct(productId, ProductStatus.SOLD)

        every { productRepository.findById(ProductId(productId)) } returns product
        every { favoriteRepository.findByCustomerId(CustomerId(customerId)) } returns null

        // When / Then
        assertThrows<BusinessRuleViolationException> {
            useCase.execute(AddToFavoriteCommand(customerId = customerId, productId = productId))
        }
    }

    @Test
    fun `PRIVATEの商品を追加するとBusinessRuleViolationExceptionが発生する`() {
        // Given
        val customerId = UUID.randomUUID()
        val productId = UUID.randomUUID()
        val product = createProduct(productId, ProductStatus.PRIVATE)

        every { productRepository.findById(ProductId(productId)) } returns product
        every { favoriteRepository.findByCustomerId(CustomerId(customerId)) } returns null

        // When / Then
        assertThrows<BusinessRuleViolationException> {
            useCase.execute(AddToFavoriteCommand(customerId = customerId, productId = productId))
        }
    }

    @Test
    fun `既にお気に入りにある商品を追加するとBusinessRuleViolationExceptionが発生する`() {
        // Given
        val customerId = UUID.randomUUID()
        val productId = UUID.randomUUID()
        val product = createProduct(productId, ProductStatus.ON_SALE)
        val existingFavorite = Favorite(
            id = FavoriteId(UUID.randomUUID()),
            customerId = CustomerId(customerId),
            items = listOf(FavoriteItem(productId = ProductId(productId), addedAt = Clock.System.now())),
        )

        every { productRepository.findById(ProductId(productId)) } returns product
        every { favoriteRepository.findByCustomerId(CustomerId(customerId)) } returns existingFavorite

        // When / Then
        assertThrows<BusinessRuleViolationException> {
            useCase.execute(AddToFavoriteCommand(customerId = customerId, productId = productId))
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
