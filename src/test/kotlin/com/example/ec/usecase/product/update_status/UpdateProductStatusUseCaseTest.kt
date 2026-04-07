package com.example.ec.usecase.product.update_status

import com.example.ec.domain.exception.BusinessRuleViolationException
import com.example.ec.domain.exception.DomainValidationException
import com.example.ec.domain.exception.ResourceNotFoundException
import com.example.ec.domain.product.*
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID
import kotlin.test.assertEquals

class UpdateProductStatusUseCaseTest {

    private val productRepository: ProductRepository = mockk()
    private lateinit var useCase: UpdateProductStatusUseCase

    @BeforeEach
    fun setUp() {
        useCase = UpdateProductStatusUseCase(productRepository)
    }

    @Test
    fun `PRIVATEからON_SALEへのステータス変更ができる`() {
        // Given
        val productId = UUID.randomUUID()
        val product = createProduct(productId, ProductStatus.PRIVATE)
        val command = UpdateProductStatusCommand(productId = productId, status = "ON_SALE")
        every { productRepository.findById(ProductId(productId)) } returns product
        every { productRepository.update(any()) } just Runs

        // When
        val result = useCase.execute(command)

        // Then
        assertEquals("ON_SALE", result.status)
        verify { productRepository.update(any()) }
    }

    @Test
    fun `ON_SALEからPRIVATEへのステータス変更ができる`() {
        // Given
        val productId = UUID.randomUUID()
        val product = createProduct(productId, ProductStatus.ON_SALE)
        val command = UpdateProductStatusCommand(productId = productId, status = "PRIVATE")
        every { productRepository.findById(ProductId(productId)) } returns product
        every { productRepository.update(any()) } just Runs

        // When
        val result = useCase.execute(command)

        // Then
        assertEquals("PRIVATE", result.status)
    }

    @Test
    fun `存在しない商品ではResourceNotFoundExceptionが発生する`() {
        // Given
        val productId = UUID.randomUUID()
        val command = UpdateProductStatusCommand(productId = productId, status = "ON_SALE")
        every { productRepository.findById(ProductId(productId)) } returns null

        // When / Then
        assertThrows<ResourceNotFoundException> { useCase.execute(command) }
    }

    @Test
    fun `無効なステータス文字列ではDomainValidationExceptionが発生する`() {
        // Given
        val productId = UUID.randomUUID()
        val product = createProduct(productId, ProductStatus.PRIVATE)
        val command = UpdateProductStatusCommand(productId = productId, status = "INVALID")
        every { productRepository.findById(ProductId(productId)) } returns product

        // When / Then
        assertThrows<DomainValidationException> { useCase.execute(command) }
    }

    @Test
    fun `SOLDからの変更はBusinessRuleViolationExceptionが発生する`() {
        // Given
        val productId = UUID.randomUUID()
        val product = createProduct(productId, ProductStatus.SOLD)
        val command = UpdateProductStatusCommand(productId = productId, status = "ON_SALE")
        every { productRepository.findById(ProductId(productId)) } returns product

        // When / Then
        assertThrows<BusinessRuleViolationException> { useCase.execute(command) }
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
