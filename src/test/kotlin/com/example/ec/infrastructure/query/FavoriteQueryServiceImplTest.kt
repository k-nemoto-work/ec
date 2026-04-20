package com.example.ec.infrastructure.query

import com.example.ec.infrastructure.table.CategoriesTable
import com.example.ec.infrastructure.table.FavoriteItemsTable
import com.example.ec.infrastructure.table.FavoritesTable
import com.example.ec.infrastructure.table.ProductsTable
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
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
class FavoriteQueryServiceImplTest {

    @Autowired
    lateinit var favoriteQueryService: FavoriteQueryServiceImpl

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
            FavoriteItemsTable.deleteAll()
            FavoritesTable.deleteAll()
            ProductsTable.deleteAll()
            CategoriesTable.deleteAll()
        }
    }

    private fun insertProduct(name: String, status: String = "ON_SALE"): UUID {
        val id = UUID.randomUUID()
        val now = Clock.System.now()
        transaction {
            ProductsTable.insert {
                it[ProductsTable.id] = id
                it[ProductsTable.name] = name
                it[ProductsTable.price] = 2000L
                it[ProductsTable.description] = "説明"
                it[ProductsTable.categoryId] = categoryId
                it[ProductsTable.status] = status
                it[ProductsTable.createdAt] = now
                it[ProductsTable.updatedAt] = now
            }
        }
        return id
    }

    private fun insertFavoriteWithItems(customerId: UUID, productIds: List<UUID>): UUID {
        val favoriteId = UUID.randomUUID()
        val now = Clock.System.now()
        transaction {
            FavoritesTable.insert {
                it[id] = favoriteId
                it[FavoritesTable.customerId] = customerId
            }
            productIds.forEach { productId ->
                FavoriteItemsTable.insert {
                    it[FavoriteItemsTable.favoriteId] = favoriteId
                    it[FavoriteItemsTable.productId] = productId
                    it[addedAt] = now
                }
            }
        }
        return favoriteId
    }

    @Test
    fun `お気に入りと商品情報をJOINで取得できる`() {
        val customerId = UUID.randomUUID()
        val productId = insertProduct("お気に入り商品")
        insertFavoriteWithItems(customerId, listOf(productId))

        val result = favoriteQueryService.findByCustomerId(customerId)

        assertEquals(1, result.items.size)
        assertEquals(productId, result.items[0].productId)
        assertEquals("お気に入り商品", result.items[0].productName)
    }

    @Test
    fun `お気に入りが存在しない場合はfavoriteId=nullで空リストを返す`() {
        val customerId = UUID.randomUUID()

        val result = favoriteQueryService.findByCustomerId(customerId)

        assertNull(result.favoriteId)
        assertEquals(0, result.items.size)
    }

    @Test
    fun `削除された商品はレスポンスに含まれない`() {
        val customerId = UUID.randomUUID()
        val existingProductId = insertProduct("存在する商品")
        val deletedProductId = UUID.randomUUID() // DBに存在しない商品ID
        val favoriteId = UUID.randomUUID()
        val now = Clock.System.now()

        transaction {
            FavoritesTable.insert {
                it[id] = favoriteId
                it[FavoritesTable.customerId] = customerId
            }
            // 存在する商品
            FavoriteItemsTable.insert {
                it[FavoriteItemsTable.favoriteId] = favoriteId
                it[FavoriteItemsTable.productId] = existingProductId
                it[addedAt] = now
            }
            // 存在しない商品（削除済み相当: FKなしのため挿入可能）
            FavoriteItemsTable.insert {
                it[FavoriteItemsTable.favoriteId] = favoriteId
                it[FavoriteItemsTable.productId] = deletedProductId
                it[addedAt] = now
            }
        }

        val result = favoriteQueryService.findByCustomerId(customerId)

        assertEquals(1, result.items.size)
        assertEquals(existingProductId, result.items[0].productId)
    }
}
