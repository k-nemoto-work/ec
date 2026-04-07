package com.example.ec.domain.product

import com.example.ec.domain.exception.DomainValidationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class MoneyTest {

    @Test
    fun `正の金額でMoneyを作成できる`() {
        val money = Money(1000)
        assertEquals(1000L, money.amount)
    }

    @Test
    fun `1円でMoneyを作成できる`() {
        val money = Money(1)
        assertEquals(1L, money.amount)
    }

    @Test
    fun `0円ではDomainValidationExceptionが発生する`() {
        assertThrows<DomainValidationException> { Money(0) }
    }

    @Test
    fun `負の金額ではDomainValidationExceptionが発生する`() {
        assertThrows<DomainValidationException> { Money(-100) }
    }

    @Test
    fun `2つのMoneyを加算できる`() {
        val a = Money(1000)
        val b = Money(500)
        assertEquals(Money(1500), a + b)
    }
}
