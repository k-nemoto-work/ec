package com.example.ec.infrastructure.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object FavoriteItemsTable : Table("favorite_items") {
    val favoriteId = uuid("favorite_id")
    val productId = uuid("product_id")
    val addedAt = timestamp("added_at")

    override val primaryKey = PrimaryKey(favoriteId, productId)
}
