package com.example.ec.usecase.product.register

import com.example.ec.domain.exception.DomainValidationException
import com.example.ec.domain.exception.ResourceNotFoundException
import com.example.ec.domain.product.*
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID
import kotlin.test.assertNotNull

class RegisterProductUseCaseTest {

    private val productRepository: ProductRepository = mockk()
    private val categoryRepository: CategoryRepository = mockk()
    private lateinit var useCase: RegisterProductUseCase

    @BeforeEach
    fun setUp() {
        useCase = RegisterProductUseCase(productRepository, categoryRepository)
    }

    @Test
    fun `正常に商品を登録できる`() {
        // Given
        val categoryId = UUID.randomUUID()
        val command = RegisterProductCommand(
            name = "テスト商品",
            price = 1000L,
            description = "説明文",
            categoryId = categoryId,
        )
        every { categoryRepository.findById(CategoryId(categoryId)) } returns Category(CategoryId(categoryId), "古着")
        every { productRepository.save(any()) } just Runs

        // When
        val productId = useCase.execute(command)

        // Then
        assertNotNull(productId)
        verify { productRepository.save(any()) }
    }

    @Test
    fun `存在しないカテゴリではResourceNotFoundExceptionが発生する`() {
        // Given
        val categoryId = UUID.randomUUID()
        val command = RegisterProductCommand(
            name = "テスト商品",
            price = 1000L,
            description = "",
            categoryId = categoryId,
        )
        every { categoryRepository.findById(CategoryId(categoryId)) } returns null

        // When / Then
        assertThrows<ResourceNotFoundException> { useCase.execute(command) }
    }

    @Test
    fun `価格が0以下ではDomainValidationExceptionが発生する`() {
        // Given
        val categoryId = UUID.randomUUID()
        val command = RegisterProductCommand(
            name = "テスト商品",
            price = 0L,
            description = "",
            categoryId = categoryId,
        )
        every { categoryRepository.findById(CategoryId(categoryId)) } returns Category(CategoryId(categoryId), "古着")

        // When / Then
        assertThrows<DomainValidationException> { useCase.execute(command) }
    }
}
