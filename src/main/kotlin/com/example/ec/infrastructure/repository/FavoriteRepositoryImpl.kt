package com.example.ec.infrastructure.repository

import com.example.ec.domain.customer.CustomerId
import com.example.ec.domain.customer.Favorite
import com.example.ec.domain.customer.FavoriteId
import com.example.ec.domain.customer.FavoriteItem
import com.example.ec.domain.customer.FavoriteRepository
import com.example.ec.domain.product.ProductId
import com.example.ec.infrastructure.table.FavoriteItemsTable
import com.example.ec.infrastructure.table.FavoritesTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.and
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
                .orderBy(FavoriteItemsTable.addedAt, SortOrder.ASC)
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
            // お気に入りレコードが未存在の場合のみ INSERT
            val exists = FavoritesTable
                .selectAll().where { FavoritesTable.id eq favorite.id.value }
                .count() > 0

            if (!exists) {
                FavoritesTable.insert {
                    it[id] = favorite.id.value
                    it[customerId] = favorite.customerId.value
                }
            }

            // 差分更新: 現在のDBアイテムと比較して追加・削除のみ実行
            val currentProductIds = FavoriteItemsTable
                .selectAll().where { FavoriteItemsTable.favoriteId eq favorite.id.value }
                .map { it[FavoriteItemsTable.productId] }
                .toSet()

            val desiredProductIds = favorite.items.map { it.productId.value }.toSet()

            val toDelete = currentProductIds - desiredProductIds
            if (toDelete.isNotEmpty()) {
                FavoriteItemsTable.deleteWhere {
                    (favoriteId eq favorite.id.value) and (productId inList toDelete.toList())
                }
            }

            val toInsert = favorite.items.filter { it.productId.value !in currentProductIds }
            toInsert.forEach { item ->
                FavoriteItemsTable.insert {
                    it[favoriteId] = favorite.id.value
                    it[productId] = item.productId.value
                    it[addedAt] = item.addedAt
                }
            }
        }
    }
}
