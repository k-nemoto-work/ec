package com.example.ec.usecase.order.update_payment

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
import com.example.ec.domain.product.Money
import com.example.ec.domain.product.ProductId
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.datetime.Clock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class UpdatePaymentUseCaseTest {

    private val orderRepository: OrderRepository = mockk()
    private lateinit var useCase: UpdatePaymentUseCase

    @BeforeEach
    fun setUp() {
        useCase = UpdatePaymentUseCase(orderRepository)
    }

    private val shippingAddress = Address(
        postalCode = "123-4567",
        prefecture = "東京都",
        city = "渋谷区",
        streetAddress = "1-1-1",
    )

    private fun createOrder(customerId: UUID, paymentStatus: PaymentStatus = PaymentStatus.UNPAID): Order =
        Order(
            id = OrderId(UUID.randomUUID()),
            customerId = CustomerId(customerId),
            items = listOf(
                OrderItem(productId = ProductId(UUID.randomUUID()), productNameSnapshot = "商品", priceSnapshot = Money(1000))
            ),
            totalAmount = Money(1000),
            status = OrderStatus.PENDING,
            payment = Payment(method = PaymentMethod.CREDIT_CARD, status = paymentStatus),
            shipment = Shipment(address = shippingAddress, status = ShipmentStatus.NOT_SHIPPED),
            orderedAt = Clock.System.now(),
        )

    @Test
    fun `UNPAID状態をPAIDに変更できる`() {
        val customerId = UUID.randomUUID()
        val order = createOrder(customerId)

        every { orderRepository.findById(order.id) } returns order
        justRun { orderRepository.save(any()) }

        useCase.execute(UpdatePaymentCommand(orderId = order.id.value, customerId = customerId))

        verify(exactly = 1) { orderRepository.save(match { it.payment.status == PaymentStatus.PAID }) }
    }

    @Test
    fun `既にPAIDの場合はBusinessRuleViolationExceptionが発生する`() {
        val customerId = UUID.randomUUID()
        val order = createOrder(customerId, PaymentStatus.PAID)

        every { orderRepository.findById(order.id) } returns order

        assertThrows<BusinessRuleViolationException> {
            useCase.execute(UpdatePaymentCommand(orderId = order.id.value, customerId = customerId))
        }
    }
}
