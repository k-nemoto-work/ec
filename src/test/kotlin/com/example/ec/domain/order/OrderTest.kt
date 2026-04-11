package com.example.ec.domain.order

import com.example.ec.domain.customer.Address
import com.example.ec.domain.customer.CustomerId
import com.example.ec.domain.exception.BusinessRuleViolationException
import com.example.ec.domain.product.CategoryId
import com.example.ec.domain.product.Money
import com.example.ec.domain.product.Product
import com.example.ec.domain.product.ProductId
import com.example.ec.domain.product.ProductName
import com.example.ec.domain.product.ProductStatus
import kotlinx.datetime.Clock
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID
import kotlin.test.assertEquals

class OrderTest {

    private val customerId = CustomerId(UUID.randomUUID())
    private val shippingAddress = Address(
        postalCode = "123-4567",
        prefecture = "東京都",
        city = "渋谷区",
        streetAddress = "1-1-1",
    )

    private fun createProduct(status: ProductStatus = ProductStatus.ON_SALE): Product = Product(
        id = ProductId(UUID.randomUUID()),
        name = ProductName("テスト商品"),
        price = Money(1000),
        description = "テスト説明",
        categoryId = CategoryId(UUID.randomUUID()),
        status = status,
    )

    private fun createOrder(status: OrderStatus = OrderStatus.CONFIRMED): Order {
        val product = createProduct()
        val order = Order.create(
            customerId = customerId,
            products = listOf(product),
            shippingAddress = shippingAddress,
            paymentMethod = PaymentMethod.CREDIT_CARD,
        )
        return when (status) {
            OrderStatus.CONFIRMED -> order
            OrderStatus.SHIPPING -> order.copy(status = OrderStatus.SHIPPING)
            OrderStatus.DELIVERED -> order.copy(status = OrderStatus.DELIVERED)
            OrderStatus.CANCELLED -> order.copy(status = OrderStatus.CANCELLED)
        }
    }

    // === Order.create() ===

    @Test
    fun `ON_SALEの商品から注文を作成できる`() {
        val product = createProduct(ProductStatus.ON_SALE)
        val order = Order.create(
            customerId = customerId,
            products = listOf(product),
            shippingAddress = shippingAddress,
            paymentMethod = PaymentMethod.CREDIT_CARD,
        )

        assertEquals(1, order.items.size)
        assertEquals(product.id, order.items[0].productId)
        assertEquals(product.name.value, order.items[0].productNameSnapshot)
        assertEquals(product.price, order.items[0].priceSnapshot)
        assertEquals(product.price.amount, order.totalAmount.amount)
        assertEquals(OrderStatus.CONFIRMED, order.status)
        assertEquals(PaymentStatus.UNPAID, order.payment.status)
        assertEquals(ShipmentStatus.NOT_SHIPPED, order.shipment.status)
    }

    @Test
    fun `商品が空の場合はBusinessRuleViolationExceptionが発生する`() {
        assertThrows<BusinessRuleViolationException> {
            Order.create(
                customerId = customerId,
                products = emptyList(),
                shippingAddress = shippingAddress,
                paymentMethod = PaymentMethod.CREDIT_CARD,
            )
        }
    }

    @Test
    fun `ON_SALE以外の商品が含まれる場合はBusinessRuleViolationExceptionが発生する`() {
        val reservedProduct = createProduct(ProductStatus.RESERVED)
        assertThrows<BusinessRuleViolationException> {
            Order.create(
                customerId = customerId,
                products = listOf(reservedProduct),
                shippingAddress = shippingAddress,
                paymentMethod = PaymentMethod.CREDIT_CARD,
            )
        }
    }

    @Test
    fun `複数商品の合計金額が正しく計算される`() {
        val product1 = createProduct().copy(price = Money(1000))
        val product2 = createProduct().copy(price = Money(2000))
        val order = Order.create(
            customerId = customerId,
            products = listOf(product1, product2),
            shippingAddress = shippingAddress,
            paymentMethod = PaymentMethod.CREDIT_CARD,
        )
        assertEquals(3000, order.totalAmount.amount)
    }

    // === Order.cancel() ===

    @Test
    fun `CONFIRMED状態の注文をキャンセルできる`() {
        val order = createOrder(OrderStatus.CONFIRMED)
        val cancelled = order.cancel()
        assertEquals(OrderStatus.CANCELLED, cancelled.status)
    }

    @Test
    fun `SHIPPING状態の注文はキャンセルできない`() {
        val order = createOrder(OrderStatus.SHIPPING)
        assertThrows<BusinessRuleViolationException> {
            order.cancel()
        }
    }

    @Test
    fun `DELIVERED状態の注文はキャンセルできない`() {
        val order = createOrder(OrderStatus.DELIVERED)
        assertThrows<BusinessRuleViolationException> {
            order.cancel()
        }
    }

    // === Order.updatePayment() ===

    @Test
    fun `UNPAID状態の注文をPAIDに変更できる`() {
        val order = createOrder()
        assertEquals(PaymentStatus.UNPAID, order.payment.status)
        val updated = order.updatePayment()
        assertEquals(PaymentStatus.PAID, updated.payment.status)
    }

    @Test
    fun `既にPAID状態の注文はBusinessRuleViolationExceptionが発生する`() {
        val order = createOrder().updatePayment()
        assertThrows<BusinessRuleViolationException> {
            order.updatePayment()
        }
    }

    // === Order.updateShipment() ===

    @Test
    fun `PAIDの注文でNOT_SHIPPEDからSHIPPEDに変更できる`() {
        val order = createOrder().updatePayment()
        val updated = order.updateShipment(ShipmentStatus.SHIPPED)
        assertEquals(ShipmentStatus.SHIPPED, updated.shipment.status)
        assertEquals(OrderStatus.SHIPPING, updated.status)
    }

    @Test
    fun `SHIPPEDからDELIVEREDに変更できる`() {
        val order = createOrder().updatePayment().updateShipment(ShipmentStatus.SHIPPED)
        val updated = order.updateShipment(ShipmentStatus.DELIVERED)
        assertEquals(ShipmentStatus.DELIVERED, updated.shipment.status)
        assertEquals(OrderStatus.DELIVERED, updated.status)
    }

    @Test
    fun `UNPAIDの場合SHIPPEDに変更するとBusinessRuleViolationExceptionが発生する`() {
        val order = createOrder()
        assertEquals(PaymentStatus.UNPAID, order.payment.status)
        assertThrows<BusinessRuleViolationException> {
            order.updateShipment(ShipmentStatus.SHIPPED)
        }
    }

    @Test
    fun `DELIVERED後は配送ステータスを変更できない`() {
        val order = createOrder()
            .updatePayment()
            .updateShipment(ShipmentStatus.SHIPPED)
            .updateShipment(ShipmentStatus.DELIVERED)
        assertThrows<BusinessRuleViolationException> {
            order.updateShipment(ShipmentStatus.SHIPPED)
        }
    }
}
