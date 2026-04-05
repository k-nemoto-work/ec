package com.example.ec.domain.customer

import com.example.ec.domain.exception.DomainValidationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class CustomerNameTest {

    @Test
    fun `正常な顧客名を生成できる`() {
        val name = CustomerName("田中太郎")
        assertEquals("田中太郎", name.value)
    }

    @Test
    fun `最大文字数ちょうどの顧客名を生成できる`() {
        assertDoesNotThrow { CustomerName("a".repeat(CustomerName.MAX_LENGTH)) }
    }

    @Test
    fun `顧客名が空の場合はエラーになる`() {
        assertThrows<DomainValidationException> { CustomerName("") }
    }

    @Test
    fun `顧客名が空白のみの場合はエラーになる`() {
        assertThrows<DomainValidationException> { CustomerName("   ") }
    }

    @Test
    fun `顧客名が最大文字数を超える場合はエラーになる`() {
        assertThrows<DomainValidationException> { CustomerName("a".repeat(CustomerName.MAX_LENGTH + 1)) }
    }
}
