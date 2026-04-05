package com.example.ec.usecase.customer.update_address

import com.example.ec.domain.customer.*
import com.example.ec.domain.exception.ResourceNotFoundException
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class UpdateAddressUseCaseTest {

    private val customerRepository: CustomerRepository = mockk()

    private lateinit var useCase: UpdateAddressUseCase

    @BeforeEach
    fun setUp() {
        useCase = UpdateAddressUseCase(customerRepository)
    }

    @Test
    fun `正常に住所を更新できる`() {
        // Given
        val customerId = UUID.randomUUID()
        val customer = Customer(
            id = CustomerId(customerId),
            name = "田中太郎",
            email = Email("tanaka@example.com"),
            passwordHash = "hashed_password",
            status = CustomerStatus.ACTIVE,
            address = null,
        )
        val command = UpdateAddressCommand(
            customerId = customerId,
            postalCode = "123-4567",
            prefecture = "東京都",
            city = "渋谷区",
            streetAddress = "渋谷1-2-3",
        )
        every { customerRepository.findById(CustomerId(customerId)) } returns customer
        every { customerRepository.update(any()) } just Runs

        // When
        useCase.execute(command)

        // Then
        verify { customerRepository.update(any()) }
    }

    @Test
    fun `存在しない顧客の住所更新はエラーになる`() {
        // Given
        val customerId = UUID.randomUUID()
        val command = UpdateAddressCommand(
            customerId = customerId,
            postalCode = "123-4567",
            prefecture = "東京都",
            city = "渋谷区",
            streetAddress = "渋谷1-2-3",
        )
        every { customerRepository.findById(CustomerId(customerId)) } returns null

        // When / Then
        assertThrows<ResourceNotFoundException> { useCase.execute(command) }
    }
}
