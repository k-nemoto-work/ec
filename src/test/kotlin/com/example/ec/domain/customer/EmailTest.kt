package com.example.ec.domain.customer

import com.example.ec.domain.exception.DomainValidationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class EmailTest {

    @Test
    fun `正常なメールアドレスで生成できる`() {
        val email = assertDoesNotThrow { Email("test@example.com") }
        assertEquals("test@example.com", email.value)
    }

    @Test
    fun `空文字のメールアドレスはエラーになる`() {
        assertThrows<DomainValidationException> { Email("") }
    }

    @Test
    fun `空白のみのメールアドレスはエラーになる`() {
        assertThrows<DomainValidationException> { Email("   ") }
    }

    @Test
    fun `@を含まないメールアドレスはエラーになる`() {
        assertThrows<DomainValidationException> { Email("invalid-email") }
    }

    @Test
    fun `256文字以上のメールアドレスはエラーになる`() {
        val longEmail = "a".repeat(250) + "@example.com"
        assertThrows<DomainValidationException> { Email(longEmail) }
    }
}
