package com.example.ec.usecase.customer.get_profile

import com.example.ec.domain.customer.*
import com.example.ec.domain.exception.ResourceNotFoundException
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNull

@ExtendWith(MockKExtension::class)
class GetCustomerProfileUseCaseTest {

    private val customerRepository: CustomerRepository = mockk()
    private lateinit var useCase: GetCustomerProfileUseCase

    @BeforeEach
    fun setUp() {
        useCase = GetCustomerProfileUseCase(customerRepository)
    }

    @Test
    fun `正常にプロフィールを取得できる`() {
        // Given
        val customerId = UUID.randomUUID()
        val customer = Customer(
            id = CustomerId(customerId),
            name = CustomerName("田中太郎"),
            email = Email("tanaka@example.com"),
            passwordHash = "hashed_password",
            status = CustomerStatus.ACTIVE,
            address = Address(
                postalCode = "123-4567",
                prefecture = "東京都",
                city = "渋谷区",
                streetAddress = "渋谷1-2-3",
            ),
        )
        every { customerRepository.findById(CustomerId(customerId)) } returns customer

        // When
        val result = useCase.execute(customerId)

        // Then
        assertEquals("田中太郎", result.name)
        assertEquals("tanaka@example.com", result.email)
        assertEquals("ACTIVE", result.status)
        assertEquals("123-4567", result.address?.postalCode)
    }

    @Test
    fun `住所未登録の顧客のプロフィールを取得できる`() {
        // Given
        val customerId = UUID.randomUUID()
        val customer = Customer(
            id = CustomerId(customerId),
            name = CustomerName("田中太郎"),
            email = Email("tanaka@example.com"),
            passwordHash = "hashed_password",
            status = CustomerStatus.ACTIVE,
            address = null,
        )
        every { customerRepository.findById(CustomerId(customerId)) } returns customer

        // When
        val result = useCase.execute(customerId)

        // Then
        assertNull(result.address)
    }

    @Test
    fun `存在しない顧客のプロフィール取得はエラーになる`() {
        // Given
        val customerId = UUID.randomUUID()
        every { customerRepository.findById(CustomerId(customerId)) } returns null

        // When / Then
        assertThrows<ResourceNotFoundException> { useCase.execute(customerId) }
    }
}
