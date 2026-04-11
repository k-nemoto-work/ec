package com.example.ec.infrastructure.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object OrdersTable : Table("orders") {
    val id = uuid("id")
    val customerId = uuid("customer_id")
    val totalAmount = long("total_amount")
    val status = varchar("status", 20)
    val orderedAt = timestamp("ordered_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)
}
