package com.example.ec.usecase.customer.login

import com.example.ec.domain.customer.*
import com.example.ec.domain.exception.AuthenticationException
import com.example.ec.infrastructure.config.JwtTokenProvider
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class LoginUseCaseTest {

    private val customerRepository: CustomerRepository = mockk()
    private val passwordEncoder: PasswordEncoder = mockk()
    private val jwtTokenProvider: JwtTokenProvider = mockk()

    private lateinit var useCase: LoginUseCase

    @BeforeEach
    fun setUp() {
        useCase = LoginUseCase(customerRepository, passwordEncoder, jwtTokenProvider)
    }

    private fun createActiveCustomer(): Customer = Customer(
        id = CustomerId(UUID.randomUUID()),
        name = "田中太郎",
        email = Email("tanaka@example.com"),
        passwordHash = "hashed_password",
        status = CustomerStatus.ACTIVE,
        address = null,
    )

    @Test
    fun `正常にログインできる`() {
        // Given
        val customer = createActiveCustomer()
        val command = LoginCommand(email = "tanaka@example.com", password = "Pass1234")
        every { customerRepository.findByEmail(Email("tanaka@example.com")) } returns customer
        every { passwordEncoder.matches("Pass1234", "hashed_password") } returns true
        every { jwtTokenProvider.generateToken(customer.id) } returns "jwt_token"
        every { jwtTokenProvider.getExpirationSeconds() } returns 3600

        // When
        val result = useCase.execute(command)

        // Then
        assertNotNull(result)
        assertEquals("jwt_token", result.accessToken)
        assertEquals(3600, result.expiresIn)
    }

    @Test
    fun `存在しないメールアドレスでログインするとエラーになる`() {
        // Given
        val command = LoginCommand(email = "unknown@example.com", password = "Pass1234")
        every { customerRepository.findByEmail(Email("unknown@example.com")) } returns null

        // When / Then
        assertThrows<AuthenticationException> { useCase.execute(command) }
    }

    @Test
    fun `パスワードが不正な場合はエラーになる`() {
        // Given
        val customer = createActiveCustomer()
        val command = LoginCommand(email = "tanaka@example.com", password = "WrongPass1")
        every { customerRepository.findByEmail(Email("tanaka@example.com")) } returns customer
        every { passwordEncoder.matches("WrongPass1", "hashed_password") } returns false

        // When / Then
        assertThrows<AuthenticationException> { useCase.execute(command) }
    }

    @Test
    fun `非活性の顧客はログインできない`() {
        // Given
        val inactiveCustomer = createActiveCustomer().copy(status = CustomerStatus.INACTIVE)
        val command = LoginCommand(email = "tanaka@example.com", password = "Pass1234")
        every { customerRepository.findByEmail(Email("tanaka@example.com")) } returns inactiveCustomer
        every { passwordEncoder.matches("Pass1234", "hashed_password") } returns true

        // When / Then
        assertThrows<AuthenticationException> { useCase.execute(command) }
    }
}
