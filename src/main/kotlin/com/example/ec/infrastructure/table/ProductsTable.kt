package com.example.ec.infrastructure.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object ProductsTable : Table("products") {
    val id = uuid("id")
    val name = varchar("name", 100)
    val price = long("price")
    val description = text("description")
    val categoryId = uuid("category_id")
    val status = varchar("status", 20)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)
}
