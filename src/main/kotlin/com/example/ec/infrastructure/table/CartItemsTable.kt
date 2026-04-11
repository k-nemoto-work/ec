package com.example.ec.infrastructure.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object CartItemsTable : Table("cart_items") {
    val cartId = uuid("cart_id")
    val productId = uuid("product_id")
    val addedAt = timestamp("added_at")

    override val primaryKey = PrimaryKey(cartId, productId)
}
