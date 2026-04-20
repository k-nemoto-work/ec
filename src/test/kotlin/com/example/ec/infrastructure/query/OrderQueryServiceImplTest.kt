package com.example.ec.infrastructure.query

import com.example.ec.infrastructure.table.CategoriesTable
import com.example.ec.infrastructure.table.CustomersTable
import com.example.ec.infrastructure.table.OrderItemsTable
import com.example.ec.infrastructure.table.OrdersTable
import com.example.ec.infrastructure.table.ProductsTable
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.UUID

@SpringBootTest
@Testcontainers
class OrderQueryServiceImplTest {

    @Autowired
    lateinit var orderQueryService: OrderQueryServiceImpl

    companion object {
        @Container
        val postgres = PostgreSQLContainer<Nothing>("postgres:16").apply {
            withDatabaseName("ec_test")
            withUsername("ec_user")
            withPassword("ec_pass")
        }

        @JvmStatic
        @DynamicPropertySource
        fun configure(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }

    private val categoryId = UUID.randomUUID()
    private val customerId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        val now = Clock.System.now()
        transaction {
            CategoriesTable.insert {
                it[id] = categoryId
                it[name] = "テストカテゴリ"
            }
            CustomersTable.insert {
                it[id] = customerId
                it[name] = "テストユーザー"
                it[email] = "test@example.com"
                it[passwordHash] = "hash"
                it[status] = "ACTIVE"
                it[createdAt] = now
                it[updatedAt] = now
            }
        }
    }

    @AfterEach
    fun tearDown() {
        transaction {
            OrderItemsTable.deleteAll()
            OrdersTable.deleteAll()
            ProductsTable.deleteAll()
            CustomersTable.deleteAll()
            CategoriesTable.deleteAll()
        }
    }

    private fun insertProductAndOrder(itemCount: Int): UUID {
        val orderId = UUID.randomUUID()
        val now = Clock.System.now()
        transaction {
            OrdersTable.insert {
                it[id] = orderId
                it[OrdersTable.customerId] = customerId
                it[totalAmount] = 1000L * itemCount
                it[status] = "CONFIRMED"
                it[orderedAt] = now
                it[updatedAt] = now
            }
            repeat(itemCount) {
                val productId = UUID.randomUUID()
                ProductsTable.insert {
                    it[id] = productId
                    it[name] = "商品$it"
                    it[price] = 1000L
                    it[description] = "説明"
                    it[ProductsTable.categoryId] = categoryId
                    it[status] = "RESERVED"
                    it[createdAt] = now
                    it[updatedAt] = now
                }
                OrderItemsTable.insert {
                    it[OrderItemsTable.orderId] = orderId
                    it[OrderItemsTable.productId] = productId
                    it[productNameSnapshot] = "商品$it"
                    it[priceSnapshot] = 1000L
                }
            }
        }
        return orderId
    }

    @Test
    fun `注文一覧とitemCountを正しく取得できる`() {
        insertProductAndOrder(itemCount = 2)
        insertProductAndOrder(itemCount = 3)

        val result = orderQueryService.findOrderSummariesByCustomer(customerId, 0, 20)

        assertEquals(2, result.orders.size)
        assertEquals(2L, result.totalCount)
        val itemCounts = result.orders.map { it.itemCount }.toSet()
        assertEquals(setOf(2, 3), itemCounts)
    }

    @Test
    fun `注文がない場合は空リストを返す`() {
        val result = orderQueryService.findOrderSummariesByCustomer(customerId, 0, 20)

        assertEquals(0, result.orders.size)
        assertEquals(0L, result.totalCount)
    }

    @Test
    fun `ページネーションが正しく動作する`() {
        repeat(5) { insertProductAndOrder(itemCount = 1) }

        val result = orderQueryService.findOrderSummariesByCustomer(customerId, page = 1, size = 2)

        assertEquals(2, result.orders.size)
        assertEquals(5L, result.totalCount)
    }
}
