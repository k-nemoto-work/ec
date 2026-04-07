package com.example.ec.infrastructure.table

import org.jetbrains.exposed.sql.Table

object CategoriesTable : Table("categories") {
    val id = uuid("id")
    val name = varchar("name", 100)

    override val primaryKey = PrimaryKey(id)
}
