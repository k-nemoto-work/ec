package com.example.ec.usecase.order.get

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals

class GetOrdersUseCaseTest {

    private val orderQueryService: OrderQueryService = mockk()
    private lateinit var useCase: GetOrdersUseCase

    @BeforeEach
    fun setUp() {
        useCase = GetOrdersUseCase(orderQueryService)
    }

    @Test
    fun `注文一覧を取得できる`() {
        // Given
        val customerId = UUID.randomUUID()
        val result = OrdersResult(
            orders = listOf(
                OrderSummaryResult(
                    orderId = UUID.randomUUID(),
                    totalAmount = 5000L,
                    status = "CONFIRMED",
                    itemCount = 2,
                    orderedAt = "2026-01-01T00:00:00Z",
                ),
            ),
            totalCount = 1L,
            page = 0,
            size = 20,
        )
        every { orderQueryService.findOrderSummariesByCustomer(customerId, 0, 20) } returns result

        // When
        val actual = useCase.execute(customerId, 0, 20)

        // Then
        assertEquals(1, actual.orders.size)
        assertEquals(1L, actual.totalCount)
        assertEquals(2, actual.orders[0].itemCount)
    }

    @Test
    fun `注文がない場合、空リストを返す`() {
        // Given
        val customerId = UUID.randomUUID()
        every { orderQueryService.findOrderSummariesByCustomer(customerId, 0, 20) } returns OrdersResult(
            orders = emptyList(),
            totalCount = 0L,
            page = 0,
            size = 20,
        )

        // When
        val actual = useCase.execute(customerId, 0, 20)

        // Then
        assertEquals(0, actual.orders.size)
        assertEquals(0L, actual.totalCount)
    }
}
