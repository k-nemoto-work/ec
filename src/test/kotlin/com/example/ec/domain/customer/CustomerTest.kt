package com.example.ec.domain.customer

import com.example.ec.domain.exception.DomainValidationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CustomerTest {

    @Test
    fun `正常な顧客を生成できる`() {
        val customer = Customer.create(
            name = "田中太郎",
            email = Email("tanaka@example.com"),
            passwordHash = "hashed_password",
        )

        assertEquals(CustomerName("田中太郎"), customer.name)
        assertEquals(Email("tanaka@example.com"), customer.email)
        assertEquals(CustomerStatus.ACTIVE, customer.status)
        assertNull(customer.address)
    }

    @Test
    fun `パスワードが8文字以上で英数字混在なら検証を通過する`() {
        assertDoesNotThrow { Customer.validateRawPassword("Pass1234") }
    }

    @Test
    fun `パスワードが7文字以下の場合はエラーになる`() {
        assertThrows<DomainValidationException> { Customer.validateRawPassword("Pass12") }
    }

    @Test
    fun `パスワードに英字が含まれない場合はエラーになる`() {
        assertThrows<DomainValidationException> { Customer.validateRawPassword("12345678") }
    }

    @Test
    fun `パスワードに数字が含まれない場合はエラーになる`() {
        assertThrows<DomainValidationException> { Customer.validateRawPassword("Password") }
    }

    @Test
    fun `配送先住所を更新できる`() {
        val customer = Customer.create(
            name = "田中太郎",
            email = Email("tanaka@example.com"),
            passwordHash = "hashed_password",
        )
        val address = Address(
            postalCode = "123-4567",
            prefecture = "東京都",
            city = "渋谷区",
            streetAddress = "渋谷1-2-3",
        )

        val updated = customer.updateAddress(address)

        assertEquals(address, updated.address)
    }
}
