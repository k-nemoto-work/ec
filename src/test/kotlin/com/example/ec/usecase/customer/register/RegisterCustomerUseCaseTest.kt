package com.example.ec.usecase.customer.register

import com.example.ec.domain.customer.CustomerRepository
import com.example.ec.domain.customer.Email
import com.example.ec.domain.exception.BusinessRuleViolationException
import com.example.ec.domain.exception.DomainValidationException
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.crypto.password.PasswordEncoder
import kotlin.test.assertNotNull

class RegisterCustomerUseCaseTest {

    private val customerRepository: CustomerRepository = mockk()
    private val passwordEncoder: PasswordEncoder = mockk()

    private lateinit var useCase: RegisterCustomerUseCase

    @BeforeEach
    fun setUp() {
        useCase = RegisterCustomerUseCase(customerRepository, passwordEncoder)
    }

    @Test
    fun `正常に顧客を登録できる`() {
        // Given
        val command = RegisterCustomerCommand(
            name = "田中太郎",
            email = "tanaka@example.com",
            password = "Pass1234",
        )
        every { customerRepository.findByEmail(Email("tanaka@example.com")) } returns null
        every { passwordEncoder.encode("Pass1234") } returns "hashed_password"
        every { customerRepository.save(any()) } just Runs

        // When
        val customerId = useCase.execute(command)

        // Then
        assertNotNull(customerId)
        verify { customerRepository.save(any()) }
    }

    @Test
    fun `登録済みメールアドレスで登録するとエラーになる`() {
        // Given
        val command = RegisterCustomerCommand(
            name = "田中太郎",
            email = "existing@example.com",
            password = "Pass1234",
        )
        every { customerRepository.findByEmail(Email("existing@example.com")) } returns mockk()

        // When / Then
        assertThrows<BusinessRuleViolationException> { useCase.execute(command) }
    }

    @Test
    fun `パスワードが短すぎるとエラーになる`() {
        // Given
        val command = RegisterCustomerCommand(
            name = "田中太郎",
            email = "tanaka@example.com",
            password = "Pass1",
        )

        // When / Then
        assertThrows<DomainValidationException> { useCase.execute(command) }
    }
}
