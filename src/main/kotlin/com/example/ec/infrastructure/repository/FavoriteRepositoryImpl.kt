package com.example.ec.infrastructure.repository

import com.example.ec.domain.customer.CustomerId
import com.example.ec.domain.customer.Favorite
import com.example.ec.domain.customer.FavoriteId
import com.example.ec.domain.customer.FavoriteItem
import com.example.ec.domain.customer.FavoriteRepository
import com.example.ec.domain.product.ProductId
import com.example.ec.infrastructure.table.FavoriteItemsTable
import com.example.ec.infrastructure.table.FavoritesTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository

@Repository
class FavoriteRepositoryImpl : FavoriteRepository {

    override fun findByCustomerId(customerId: CustomerId): Favorite? {
        return transaction {
            val favoriteRow = FavoritesTable
                .selectAll().where { FavoritesTable.customerId eq customerId.value }
                .singleOrNull() ?: return@transaction null

            val favoriteId = favoriteRow[FavoritesTable.id]
            val items = FavoriteItemsTable
                .selectAll().where { FavoriteItemsTable.favoriteId eq favoriteId }
                .map { row ->
                    FavoriteItem(
                        productId = ProductId(row[FavoriteItemsTable.productId]),
                        addedAt = row[FavoriteItemsTable.addedAt],
                    )
                }

            Favorite(
                id = FavoriteId(favoriteId),
                customerId = customerId,
                items = items,
            )
        }
    }

    override fun save(favorite: Favorite) {
        transaction {
            FavoritesTable.insert {
                it[id] = favorite.id.value
                it[customerId] = favorite.customerId.value
            }
            favorite.items.forEach { item ->
                FavoriteItemsTable.insert {
                    it[favoriteId] = favorite.id.value
                    it[productId] = item.productId.value
                    it[addedAt] = item.addedAt
                }
            }
        }
    }

    override fun update(favorite: Favorite) {
        transaction {
            FavoriteItemsTable.deleteWhere { favoriteId eq favorite.id.value }
            favorite.items.forEach { item ->
                FavoriteItemsTable.insert {
                    it[favoriteId] = favorite.id.value
                    it[productId] = item.productId.value
                    it[addedAt] = item.addedAt
                }
            }
        }
    }
}
