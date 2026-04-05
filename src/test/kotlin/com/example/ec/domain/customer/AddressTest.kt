package com.example.ec.domain.customer

import com.example.ec.domain.exception.DomainValidationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class AddressTest {

    @Test
    fun `正常な住所を生成できる`() {
        val address = assertDoesNotThrow {
            Address(
                postalCode = "123-4567",
                prefecture = "東京都",
                city = "渋谷区",
                streetAddress = "渋谷1-2-3",
            )
        }
        assertEquals("123-4567", address.postalCode)
        assertEquals("東京都", address.prefecture)
    }

    @Test
    fun `郵便番号が空の場合はエラーになる`() {
        assertThrows<DomainValidationException> {
            Address(postalCode = "", prefecture = "東京都", city = "渋谷区", streetAddress = "渋谷1-2-3")
        }
    }

    @Test
    fun `都道府県が空の場合はエラーになる`() {
        assertThrows<DomainValidationException> {
            Address(postalCode = "123-4567", prefecture = "", city = "渋谷区", streetAddress = "渋谷1-2-3")
        }
    }

    @Test
    fun `市区町村が空の場合はエラーになる`() {
        assertThrows<DomainValidationException> {
            Address(postalCode = "123-4567", prefecture = "東京都", city = "", streetAddress = "渋谷1-2-3")
        }
    }

    @Test
    fun `番地が空の場合はエラーになる`() {
        assertThrows<DomainValidationException> {
            Address(postalCode = "123-4567", prefecture = "東京都", city = "渋谷区", streetAddress = "")
        }
    }
}
