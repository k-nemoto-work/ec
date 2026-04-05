package com.example.ec.infrastructure.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object CustomersTable : Table("customers") {
    val id = uuid("id")
    val name = varchar("name", 50)
    val email = varchar("email", 255)
    val passwordHash = varchar("password_hash", 255)
    val status = varchar("status", 20)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)
}
