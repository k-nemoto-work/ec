package com.example.ec.usecase.order.cancel

import com.example.ec.domain.customer.Address
import com.example.ec.domain.customer.CustomerId
import com.example.ec.domain.exception.BusinessRuleViolationException
import com.example.ec.domain.exception.UnauthorizedAccessException
import com.example.ec.domain.order.Order
import com.example.ec.domain.order.OrderId
import com.example.ec.domain.order.OrderItem
import com.example.ec.domain.order.OrderRepository
import com.example.ec.domain.order.OrderStatus
import com.example.ec.domain.order.Payment
import com.example.ec.domain.order.PaymentMethod
import com.example.ec.domain.order.PaymentStatus
import com.example.ec.domain.order.Shipment
import com.example.ec.domain.order.ShipmentStatus
import com.example.ec.domain.product.CategoryId
import com.example.ec.domain.product.Money
import com.example.ec.domain.product.Product
import com.example.ec.domain.product.ProductId
import com.example.ec.domain.product.ProductName
import com.example.ec.domain.product.ProductRepository
import com.example.ec.domain.product.ProductStatus
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.datetime.Clock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class CancelOrderUseCaseTest {

    private val orderRepository: OrderRepository = mockk()
    private val productRepository: ProductRepository = mockk()
    private lateinit var useCase: CancelOrderUseCase

    @BeforeEach
    fun setUp() {
        useCase = CancelOrderUseCase(orderRepository, productRepository)
    }

    private val shippingAddress = Address(
        postalCode = "123-4567",
        prefecture = "東京都",
        city = "渋谷区",
        streetAddress = "1-1-1",
    )

    private fun createOrder(customerId: UUID, status: OrderStatus): Order {
        val productId = ProductId(UUID.randomUUID())
        return Order(
            id = OrderId(UUID.randomUUID()),
            customerId = CustomerId(customerId),
            items = listOf(
                OrderItem(
                    productId = productId,
                    productNameSnapshot = "テスト商品",
                    priceSnapshot = Money(1000),
                )
            ),
            totalAmount = Money(1000),
            status = status,
            payment = Payment(method = PaymentMethod.CREDIT_CARD, status = PaymentStatus.UNPAID),
            shipment = Shipment(address = shippingAddress, status = ShipmentStatus.NOT_SHIPPED),
            orderedAt = Clock.System.now(),
        )
    }

    private fun createProduct(productId: ProductId): Product = Product(
        id = productId,
        name = ProductName("テスト商品"),
        price = Money(1000),
        description = "説明",
        categoryId = CategoryId(UUID.randomUUID()),
        status = ProductStatus.RESERVED,
    )

    @Test
    fun `PENDING状態の注文をキャンセルできる`() {
        val customerId = UUID.randomUUID()
        val order = createOrder(customerId, OrderStatus.PENDING)
        val product = createProduct(order.items[0].productId)

        every { orderRepository.findById(order.id) } returns order
        every { productRepository.findAllByIds(any()) } returns listOf(product)
        justRun { orderRepository.save(any()) }
        justRun { productRepository.update(any()) }

        useCase.execute(order.id.value, customerId)

        verify(exactly = 1) { orderRepository.save(match { it.status == OrderStatus.CANCELLED }) }
        verify(exactly = 1) { productRepository.update(match { it.status == ProductStatus.ON_SALE }) }
    }

    @Test
    fun `SHIPPING状態の注文はキャンセルできない`() {
        val customerId = UUID.randomUUID()
        val order = createOrder(customerId, OrderStatus.SHIPPING)

        every { orderRepository.findById(order.id) } returns order

        assertThrows<BusinessRuleViolationException> {
            useCase.execute(order.id.value, customerId)
        }
    }

    @Test
    fun `他人の注文をキャンセルするとUnauthorizedAccessExceptionが発生する`() {
        val customerId = UUID.randomUUID()
        val otherCustomerId = UUID.randomUUID()
        val order = createOrder(otherCustomerId, OrderStatus.PENDING)

        every { orderRepository.findById(order.id) } returns order

        assertThrows<UnauthorizedAccessException> {
            useCase.execute(order.id.value, customerId)
        }
    }
}
