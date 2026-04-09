package com.example.ec.infrastructure.table

import org.jetbrains.exposed.sql.Table

object CartItemsTable : Table("cart_items") {
    val cartId = uuid("cart_id")
    val productId = uuid("product_id")

    override val primaryKey = PrimaryKey(cartId, productId)
}
