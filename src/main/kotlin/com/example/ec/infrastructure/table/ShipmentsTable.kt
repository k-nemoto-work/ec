package com.example.ec.infrastructure.table

import org.jetbrains.exposed.sql.Table

object ShipmentsTable : Table("shipments") {
    val orderId = uuid("order_id")
    val postalCode = varchar("postal_code", 8)
    val prefecture = varchar("prefecture", 50)
    val city = varchar("city", 100)
    val streetAddress = varchar("street_address", 200)
    val status = varchar("status", 20)

    override val primaryKey = PrimaryKey(orderId)
}
