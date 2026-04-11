package com.example.ec.infrastructure.table

import org.jetbrains.exposed.sql.Table

object PaymentsTable : Table("payments") {
    val orderId = uuid("order_id")
    val method = varchar("method", 20)
    val status = varchar("status", 20)

    override val primaryKey = PrimaryKey(orderId)
}
