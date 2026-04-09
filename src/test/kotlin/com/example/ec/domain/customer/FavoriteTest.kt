package com.example.ec.domain.customer

import com.example.ec.domain.exception.BusinessRuleViolationException
import com.example.ec.domain.exception.ResourceNotFoundException
import com.example.ec.domain.product.ProductId
import com.example.ec.domain.product.ProductStatus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FavoriteTest {

    private val customerId = CustomerId(UUID.randomUUID())

    @Test
    fun `ON_SALEの商品をお気に入りに追加できる`() {
        val favorite = Favorite.create(customerId)
        val productId = ProductId(UUID.randomUUID())

        val updated = favorite.addItem(productId, ProductStatus.ON_SALE)

        assertEquals(1, updated.items.size)
        assertEquals(productId, updated.items[0].productId)
    }

    @Test
    fun `RESERVEDの商品をお気に入りに追加できる`() {
        val favorite = Favorite.create(customerId)
        val productId = ProductId(UUID.randomUUID())

        val updated = favorite.addItem(productId, ProductStatus.RESERVED)

        assertEquals(1, updated.items.size)
    }

    @Test
    fun `SOLDの商品をお気に入りに追加するとBusinessRuleViolationExceptionが発生する`() {
        val favorite = Favorite.create(customerId)
        val productId = ProductId(UUID.randomUUID())

        assertThrows<BusinessRuleViolationException> {
            favorite.addItem(productId, ProductStatus.SOLD)
        }
    }

    @Test
    fun `PRIVATEの商品をお気に入りに追加するとBusinessRuleViolationExceptionが発生する`() {
        val favorite = Favorite.create(customerId)
        val productId = ProductId(UUID.randomUUID())

        assertThrows<BusinessRuleViolationException> {
            favorite.addItem(productId, ProductStatus.PRIVATE)
        }
    }

    @Test
    fun `同じ商品を2回追加するとBusinessRuleViolationExceptionが発生する`() {
        val favorite = Favorite.create(customerId)
        val productId = ProductId(UUID.randomUUID())
        val withItem = favorite.addItem(productId, ProductStatus.ON_SALE)

        assertThrows<BusinessRuleViolationException> {
            withItem.addItem(productId, ProductStatus.ON_SALE)
        }
    }

    @Test
    fun `お気に入りから商品を削除できる`() {
        val favorite = Favorite.create(customerId)
        val productId = ProductId(UUID.randomUUID())
        val withItem = favorite.addItem(productId, ProductStatus.ON_SALE)

        val updated = withItem.removeItem(productId)

        assertTrue(updated.items.isEmpty())
    }

    @Test
    fun `存在しない商品を削除するとResourceNotFoundExceptionが発生する`() {
        val favorite = Favorite.create(customerId)
        val productId = ProductId(UUID.randomUUID())

        assertThrows<ResourceNotFoundException> {
            favorite.removeItem(productId)
        }
    }

    @Test
    fun `複数の商品を追加して1つだけ削除できる`() {
        val favorite = Favorite.create(customerId)
        val productId1 = ProductId(UUID.randomUUID())
        val productId2 = ProductId(UUID.randomUUID())

        val withItems = favorite
            .addItem(productId1, ProductStatus.ON_SALE)
            .addItem(productId2, ProductStatus.ON_SALE)

        val updated = withItems.removeItem(productId1)

        assertEquals(1, updated.items.size)
        assertEquals(productId2, updated.items[0].productId)
    }
}
