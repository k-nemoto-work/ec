package com.example.ec.usecase.order.update_shipment

import com.example.ec.domain.customer.Address
import com.example.ec.domain.customer.CustomerId
import com.example.ec.domain.exception.BusinessRuleViolationException
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

class UpdateShipmentUseCaseTest {

    private val orderRepository: OrderRepository = mockk()
    private val productRepository: ProductRepository = mockk()
    private lateinit var useCase: UpdateShipmentUseCase

    @BeforeEach
    fun setUp() {
        useCase = UpdateShipmentUseCase(orderRepository, productRepository)
    }

    private val shippingAddress = Address(
        postalCode = "123-4567",
        prefecture = "東京都",
        city = "渋谷区",
        streetAddress = "1-1-1",
    )
    private val productId = ProductId(UUID.randomUUID())

    private fun createOrder(
        customerId: UUID,
        paymentStatus: PaymentStatus = PaymentStatus.PAID,
        shipmentStatus: ShipmentStatus = ShipmentStatus.NOT_SHIPPED,
    ): Order = Order(
        id = OrderId(UUID.randomUUID()),
        customerId = CustomerId(customerId),
        items = listOf(
            OrderItem(productId = productId, productNameSnapshot = "商品", priceSnapshot = Money(1000))
        ),
        totalAmount = Money(1000),
        status = OrderStatus.CONFIRMED,
        payment = Payment(method = PaymentMethod.CREDIT_CARD, status = paymentStatus),
        shipment = Shipment(address = shippingAddress, status = shipmentStatus),
        orderedAt = Clock.System.now(),
    )

    @Test
    fun `NOT_SHIPPEDからSHIPPEDに変更できる`() {
        val customerId = UUID.randomUUID()
        val order = createOrder(customerId)

        every { orderRepository.findById(order.id) } returns order
        justRun { orderRepository.save(any()) }

        useCase.execute(
            UpdateShipmentCommand(orderId = order.id.value, customerId = customerId, newStatus = ShipmentStatus.SHIPPED)
        )

        verify(exactly = 1) { orderRepository.save(match { it.shipment.status == ShipmentStatus.SHIPPED }) }
    }

    @Test
    fun `DELIVERED時に商品がSOLDになる`() {
        val customerId = UUID.randomUUID()
        val order = createOrder(customerId, shipmentStatus = ShipmentStatus.SHIPPED)
        val product = Product(
            id = productId,
            name = ProductName("商品"),
            price = Money(1000),
            description = "説明",
            categoryId = CategoryId(UUID.randomUUID()),
            status = ProductStatus.RESERVED,
        )

        every { orderRepository.findById(order.id) } returns order
        every { productRepository.findAllByIds(listOf(productId)) } returns listOf(product)
        justRun { orderRepository.save(any()) }
        justRun { productRepository.update(any()) }

        useCase.execute(
            UpdateShipmentCommand(orderId = order.id.value, customerId = customerId, newStatus = ShipmentStatus.DELIVERED)
        )

        verify(exactly = 1) { orderRepository.save(match { it.shipment.status == ShipmentStatus.DELIVERED }) }
        verify(exactly = 1) { productRepository.update(match { it.status == ProductStatus.SOLD }) }
    }

    @Test
    fun `UNPAID状態でSHIPPEDに変更するとBusinessRuleViolationExceptionが発生する`() {
        val customerId = UUID.randomUUID()
        val order = createOrder(customerId, paymentStatus = PaymentStatus.UNPAID)

        every { orderRepository.findById(order.id) } returns order

        assertThrows<BusinessRuleViolationException> {
            useCase.execute(
                UpdateShipmentCommand(
                    orderId = order.id.value,
                    customerId = customerId,
                    newStatus = ShipmentStatus.SHIPPED,
                )
            )
        }
    }
}
