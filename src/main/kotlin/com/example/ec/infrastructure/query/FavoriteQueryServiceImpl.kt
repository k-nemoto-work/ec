package com.example.ec.infrastructure.query

import com.example.ec.infrastructure.table.FavoriteItemsTable
import com.example.ec.infrastructure.table.FavoritesTable
import com.example.ec.infrastructure.table.ProductsTable
import com.example.ec.usecase.favorite.get.FavoriteItemResult
import com.example.ec.usecase.favorite.get.FavoriteQueryService
import com.example.ec.usecase.favorite.get.FavoriteResult
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class FavoriteQueryServiceImpl : FavoriteQueryService {

    override fun findByCustomerId(customerId: UUID): FavoriteResult {
        return transaction {
            val rows = FavoritesTable
                .join(FavoriteItemsTable, JoinType.LEFT, FavoritesTable.id, FavoriteItemsTable.favoriteId)
                .join(ProductsTable, JoinType.LEFT, FavoriteItemsTable.productId, ProductsTable.id)
                .selectAll()
                .where { FavoritesTable.customerId eq customerId }
                .toList()

            if (rows.isEmpty()) return@transaction FavoriteResult(favoriteId = null, items = emptyList())

            val favoriteId = rows.first()[FavoritesTable.id]

            val items = rows.mapNotNull { row ->
                val productId = row.getOrNull(FavoriteItemsTable.productId) ?: return@mapNotNull null
                val productName = row.getOrNull(ProductsTable.name) ?: return@mapNotNull null
                FavoriteItemResult(
                    productId = productId,
                    productName = productName,
                    price = row[ProductsTable.price],
                    status = row[ProductsTable.status],
                    addedAt = row[FavoriteItemsTable.addedAt].toString(),
                )
            }

            FavoriteResult(favoriteId = favoriteId, items = items)
        }
    }
}
