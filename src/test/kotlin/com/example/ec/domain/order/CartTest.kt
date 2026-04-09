package com.example.ec.domain.order

import com.example.ec.domain.customer.CustomerId
import com.example.ec.domain.exception.BusinessRuleViolationException
import com.example.ec.domain.exception.ResourceNotFoundException
import com.example.ec.domain.product.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CartTest {

    private val customerId = CustomerId(UUID.randomUUID())

    @Test
    fun `ON_SALEの商品をカートに追加できる`() {
        val cart = Cart.create(customerId)
        val product = createProduct(ProductStatus.ON_SALE)

        val updated = cart.addItem(product)

        assertEquals(1, updated.items.size)
        assertEquals(product.id, updated.items[0].productId)
    }

    @Test
    fun `RESERVED商品をカートに追加するとBusinessRuleViolationExceptionが発生する`() {
        val cart = Cart.create(customerId)
        val product = createProduct(ProductStatus.RESERVED)

        assertThrows<BusinessRuleViolationException> {
            cart.addItem(product)
        }
    }

    @Test
    fun `SOLD商品をカートに追加するとBusinessRuleViolationExceptionが発生する`() {
        val cart = Cart.create(customerId)
        val product = createProduct(ProductStatus.SOLD)

        assertThrows<BusinessRuleViolationException> {
            cart.addItem(product)
        }
    }

    @Test
    fun `PRIVATE商品をカートに追加するとBusinessRuleViolationExceptionが発生する`() {
        val cart = Cart.create(customerId)
        val product = createProduct(ProductStatus.PRIVATE)

        assertThrows<BusinessRuleViolationException> {
            cart.addItem(product)
        }
    }

    @Test
    fun `同じ商品をカートに2回追加するとBusinessRuleViolationExceptionが発生する`() {
        val cart = Cart.create(customerId)
        val product = createProduct(ProductStatus.ON_SALE)
        val withItem = cart.addItem(product)

        assertThrows<BusinessRuleViolationException> {
            withItem.addItem(product)
        }
    }

    @Test
    fun `カートから商品を削除できる`() {
        val cart = Cart.create(customerId)
        val product = createProduct(ProductStatus.ON_SALE)
        val withItem = cart.addItem(product)

        val updated = withItem.removeItem(product.id)

        assertTrue(updated.items.isEmpty())
    }

    @Test
    fun `存在しない商品を削除するとResourceNotFoundExceptionが発生する`() {
        val cart = Cart.create(customerId)
        val productId = ProductId(UUID.randomUUID())

        assertThrows<ResourceNotFoundException> {
            cart.removeItem(productId)
        }
    }

    @Test
    fun `複数の商品を追加して1つだけ削除できる`() {
        val cart = Cart.create(customerId)
        val product1 = createProduct(ProductStatus.ON_SALE)
        val product2 = createProduct(ProductStatus.ON_SALE)

        val withItems = cart.addItem(product1).addItem(product2)
        val updated = withItems.removeItem(product1.id)

        assertEquals(1, updated.items.size)
        assertEquals(product2.id, updated.items[0].productId)
    }

    private fun createProduct(status: ProductStatus): Product =
        Product(
            id = ProductId(UUID.randomUUID()),
            name = ProductName("テスト商品"),
            price = Money(1000),
            description = "説明",
            categoryId = CategoryId(UUID.randomUUID()),
            status = status,
        )
}
