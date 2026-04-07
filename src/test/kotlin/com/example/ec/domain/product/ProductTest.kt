package com.example.ec.domain.product

import com.example.ec.domain.exception.BusinessRuleViolationException
import com.example.ec.domain.exception.DomainValidationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID
import kotlin.test.assertEquals

class ProductTest {

    private val categoryId = CategoryId(UUID.randomUUID())

    @Test
    fun `正常に商品を作成できる`() {
        val product = Product.create(
            name = ProductName("テスト商品"),
            price = Money(1000),
            description = "説明文",
            categoryId = categoryId,
        )
        assertEquals(ProductName("テスト商品"), product.name)
        assertEquals(Money(1000), product.price)
        assertEquals(ProductStatus.PRIVATE, product.status)
    }

    @Test
    fun `空白の商品名ではDomainValidationExceptionが発生する`() {
        assertThrows<DomainValidationException> {
            ProductName("")
        }
    }

    @Test
    fun `100文字を超える商品名ではDomainValidationExceptionが発生する`() {
        val longName = "a".repeat(101)
        assertThrows<DomainValidationException> {
            ProductName(longName)
        }
    }

    @Test
    fun `2000文字を超える説明ではDomainValidationExceptionが発生する`() {
        val longDescription = "a".repeat(2001)
        assertThrows<DomainValidationException> {
            Product.create(name = ProductName("商品名"), price = Money(1000), description = longDescription, categoryId = categoryId)
        }
    }

    @Test
    fun `PRIVATEからON_SALEへのステータス変更ができる`() {
        val product = createProduct(status = ProductStatus.PRIVATE)
        val updated = product.changeStatus(ProductStatus.ON_SALE)
        assertEquals(ProductStatus.ON_SALE, updated.status)
    }

    @Test
    fun `ON_SALEからPRIVATEへのステータス変更ができる`() {
        val product = createProduct(status = ProductStatus.ON_SALE)
        val updated = product.changeStatus(ProductStatus.PRIVATE)
        assertEquals(ProductStatus.PRIVATE, updated.status)
    }

    @Test
    fun `SOLDからのステータス変更はBusinessRuleViolationExceptionが発生する`() {
        val product = createProduct(status = ProductStatus.SOLD)
        assertThrows<BusinessRuleViolationException> {
            product.changeStatus(ProductStatus.ON_SALE)
        }
    }

    @Test
    fun `PRIVATEからRESERVEDへの変更はBusinessRuleViolationExceptionが発生する`() {
        val product = createProduct(status = ProductStatus.PRIVATE)
        assertThrows<BusinessRuleViolationException> {
            product.changeStatus(ProductStatus.RESERVED)
        }
    }

    @Test
    fun `ON_SALEの商品を更新できる`() {
        val product = createProduct(status = ProductStatus.ON_SALE)
        val updated = product.update(
            name = ProductName("新しい名前"),
            price = Money(2000),
            description = "新しい説明",
            categoryId = categoryId,
        )
        assertEquals(ProductName("新しい名前"), updated.name)
        assertEquals(Money(2000), updated.price)
    }

    @Test
    fun `RESERVED商品の更新はBusinessRuleViolationExceptionが発生する`() {
        val product = createProduct(status = ProductStatus.RESERVED)
        assertThrows<BusinessRuleViolationException> {
            product.update(name = ProductName("新しい名前"), price = Money(2000), description = "", categoryId = categoryId)
        }
    }

    private fun createProduct(status: ProductStatus): Product =
        Product(
            id = ProductId.generate(),
            name = ProductName("テスト商品"),
            price = Money(1000),
            description = "説明",
            categoryId = categoryId,
            status = status,
        )
}
