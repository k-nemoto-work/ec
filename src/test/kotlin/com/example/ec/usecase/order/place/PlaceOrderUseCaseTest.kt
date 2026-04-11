package com.example.ec.usecase.order.place

import com.example.ec.domain.customer.Address
import com.example.ec.domain.customer.CustomerId
import com.example.ec.domain.exception.BusinessRuleViolationException
import com.example.ec.domain.order.Cart
import com.example.ec.domain.order.CartId
import com.example.ec.domain.order.CartItem
import com.example.ec.domain.order.CartRepository
import com.example.ec.domain.order.OrderRepository
import com.example.ec.domain.order.PaymentMethod
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

class PlaceOrderUseCaseTest {

    private val cartRepository: CartRepository = mockk()
    private val productRepository: ProductRepository = mockk()
    private val orderRepository: OrderRepository = mockk()
    private lateinit var useCase: PlaceOrderUseCase

    @BeforeEach
    fun setUp() {
        useCase = PlaceOrderUseCase(cartRepository, productRepository, orderRepository)
    }

    private val shippingAddress = Address(
        postalCode = "123-4567",
        prefecture = "東京都",
        city = "渋谷区",
        streetAddress = "1-1-1",
    )

    private fun createProduct(productId: UUID, status: ProductStatus = ProductStatus.ON_SALE): Product =
        Product(
            id = ProductId(productId),
            name = ProductName("テスト商品"),
            price = Money(1000),
            description = "説明",
            categoryId = CategoryId(UUID.randomUUID()),
            status = status,
        )

    private fun createCart(customerId: UUID, productIds: List<UUID> = listOf(UUID.randomUUID())): Cart {
        val items = productIds.map { CartItem(productId = ProductId(it), addedAt = Clock.System.now()) }
        return Cart(id = CartId(UUID.randomUUID()), customerId = CustomerId(customerId), items = items)
    }

    @Test
    fun `カートの商品が正常に注文される`() {
        val customerId = UUID.randomUUID()
        val productId = UUID.randomUUID()
        val cart = createCart(customerId, listOf(productId))
        val product = createProduct(productId)

        every { cartRepository.findByCustomerId(CustomerId(customerId)) } returns cart
        every { productRepository.findAllByIds(listOf(ProductId(productId))) } returns listOf(product)
        justRun { orderRepository.save(any()) }
        justRun { productRepository.update(any()) }
        justRun { cartRepository.save(any()) }

        val result = useCase.execute(
            PlaceOrderCommand(
                customerId = customerId,
                shippingAddress = shippingAddress,
                paymentMethod = PaymentMethod.CREDIT_CARD,
            )
        )

        verify(exactly = 1) { orderRepository.save(any()) }
        verify(exactly = 1) { productRepository.update(any()) }
        verify(exactly = 1) { cartRepository.save(any()) }
    }

    @Test
    fun `カートが空の場合はBusinessRuleViolationExceptionが発生する`() {
        val customerId = UUID.randomUUID()
        val emptyCart = Cart(
            id = CartId(UUID.randomUUID()),
            customerId = CustomerId(customerId),
            items = emptyList(),
        )

        every { cartRepository.findByCustomerId(CustomerId(customerId)) } returns emptyCart

        assertThrows<BusinessRuleViolationException> {
            useCase.execute(
                PlaceOrderCommand(
                    customerId = customerId,
                    shippingAddress = shippingAddress,
                    paymentMethod = PaymentMethod.CREDIT_CARD,
                )
            )
        }
    }

    @Test
    fun `ON_SALE以外の商品が含まれる場合はBusinessRuleViolationExceptionが発生する`() {
        val customerId = UUID.randomUUID()
        val productId = UUID.randomUUID()
        val cart = createCart(customerId, listOf(productId))
        val reservedProduct = createProduct(productId, ProductStatus.RESERVED)

        every { cartRepository.findByCustomerId(CustomerId(customerId)) } returns cart
        every { productRepository.findAllByIds(listOf(ProductId(productId))) } returns listOf(reservedProduct)

        assertThrows<BusinessRuleViolationException> {
            useCase.execute(
                PlaceOrderCommand(
                    customerId = customerId,
                    shippingAddress = shippingAddress,
                    paymentMethod = PaymentMethod.CREDIT_CARD,
                )
            )
        }
    }
}
