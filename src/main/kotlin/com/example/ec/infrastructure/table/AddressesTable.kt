package com.example.ec.infrastructure.table

import org.jetbrains.exposed.sql.Table

object AddressesTable : Table("addresses") {
    val customerId = uuid("customer_id").references(CustomersTable.id)
    val postalCode = varchar("postal_code", 10)
    val prefecture = varchar("prefecture", 10)
    val city = varchar("city", 50)
    val streetAddress = varchar("street_address", 200)

    override val primaryKey = PrimaryKey(customerId)
}
