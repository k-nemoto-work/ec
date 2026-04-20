package com.example.ec.infrastructure.query

import com.example.ec.infrastructure.table.CategoriesTable
import com.example.ec.infrastructure.table.ProductsTable
import com.example.ec.usecase.product.list.ListProductsQuery
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
class ProductQueryServiceImplTest {

    @Autowired
    lateinit var productQueryService: ProductQueryServiceImpl

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

    @BeforeEach
    fun setUp() {
        transaction {
            CategoriesTable.insert {
                it[id] = categoryId
                it[name] = "テストカテゴリ"
            }
        }
    }

    @AfterEach
    fun tearDown() {
        transaction {
            ProductsTable.deleteAll()
            CategoriesTable.deleteAll()
        }
    }

    private fun insertProduct(name: String, status: String = "ON_SALE", catId: UUID = categoryId): UUID {
        val id = UUID.randomUUID()
        val now = Clock.System.now()
        transaction {
            ProductsTable.insert {
                it[ProductsTable.id] = id
                it[ProductsTable.name] = name
                it[ProductsTable.price] = 1000L
                it[ProductsTable.description] = "説明"
                it[ProductsTable.categoryId] = catId
                it[ProductsTable.status] = status
                it[ProductsTable.createdAt] = now
                it[ProductsTable.updatedAt] = now
            }
        }
        return id
    }

    @Test
    fun `販売中の商品一覧とtotalCountを取得できる`() {
        insertProduct("商品A")
        insertProduct("商品B")
        insertProduct("非公開商品", status = "PRIVATE")

        val result = productQueryService.findPageWithCount(ListProductsQuery())

        assertEquals(2, result.products.size)
        assertEquals(2L, result.totalCount)
    }

    @Test
    fun `カテゴリIDで絞り込める`() {
        val otherCategoryId = UUID.randomUUID()
        transaction {
            CategoriesTable.insert {
                it[id] = otherCategoryId
                it[name] = "他カテゴリ"
            }
        }
        insertProduct("対象商品", catId = categoryId)
        insertProduct("他カテゴリ商品", catId = otherCategoryId)

        val result = productQueryService.findPageWithCount(ListProductsQuery(categoryId = categoryId))

        assertEquals(1, result.products.size)
        assertEquals(1L, result.totalCount)
        assertEquals("対象商品", result.products[0].name)
    }

    @Test
    fun `商品がない場合は空リストとtotalCount=0を返す`() {
        val result = productQueryService.findPageWithCount(ListProductsQuery())

        assertEquals(0, result.products.size)
        assertEquals(0L, result.totalCount)
    }

    @Test
    fun `ページネーションが正しく動作する`() {
        repeat(5) { i -> insertProduct("商品$i") }

        val result = productQueryService.findPageWithCount(ListProductsQuery(page = 1, size = 2))

        assertEquals(2, result.products.size)
        assertEquals(5L, result.totalCount)
        assertEquals(1, result.page)
        assertEquals(2, result.size)
    }
}
