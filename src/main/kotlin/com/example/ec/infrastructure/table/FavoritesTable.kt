package com.example.ec.infrastructure.table

import org.jetbrains.exposed.sql.Table

object FavoritesTable : Table("favorites") {
    val id = uuid("id")
    val customerId = uuid("customer_id")

    override val primaryKey = PrimaryKey(id)
}
