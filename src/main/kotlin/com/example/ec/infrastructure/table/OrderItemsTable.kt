package com.example.ec.infrastructure.table

import org.jetbrains.exposed.sql.Table

object OrderItemsTable : Table("order_items") {
    val orderId = uuid("order_id")
    val productId = uuid("product_id")
    val productNameSnapshot = varchar("product_name_snapshot", 100)
    val priceSnapshot = long("price_snapshot")

    override val primaryKey = PrimaryKey(orderId, productId)
}
