package com.example.ec.infrastructure.repository

import com.example.ec.domain.customer.*
import com.example.ec.infrastructure.table.AddressesTable
import com.example.ec.infrastructure.table.CustomersTable
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository

@Repository
class CustomerRepositoryImpl : CustomerRepository {

    override fun save(customer: Customer) {
        transaction {
            val now = Clock.System.now()
            CustomersTable.insert {
                it[id] = customer.id.value
                it[name] = customer.name.value
                it[email] = customer.email.value
                it[passwordHash] = customer.passwordHash
                it[status] = customer.status.name
                it[createdAt] = now
                it[updatedAt] = now
            }
        }
    }

    override fun findById(id: CustomerId): Customer? {
        return transaction {
            (CustomersTable leftJoin AddressesTable)
                .selectAll().where { CustomersTable.id eq id.value }
                .singleOrNull()
                ?.toCustomer()
        }
    }

    override fun findByEmail(email: Email): Customer? {
        return transaction {
            (CustomersTable leftJoin AddressesTable)
                .selectAll().where { CustomersTable.email eq email.value }
                .singleOrNull()
                ?.toCustomer()
        }
    }

    override fun update(customer: Customer) {
        transaction {
            val now = Clock.System.now()
            CustomersTable.update({ CustomersTable.id eq customer.id.value }) {
                it[name] = customer.name.value
                it[email] = customer.email.value
                it[passwordHash] = customer.passwordHash
                it[status] = customer.status.name
                it[updatedAt] = now
            }

            val address = customer.address
            if (address != null) {
                val exists = AddressesTable
                    .selectAll().where { AddressesTable.customerId eq customer.id.value }
                    .count() > 0

                if (exists) {
                    AddressesTable.update({ AddressesTable.customerId eq customer.id.value }) {
                        it[postalCode] = address.postalCode
                        it[prefecture] = address.prefecture
                        it[city] = address.city
                        it[streetAddress] = address.streetAddress
                    }
                } else {
                    AddressesTable.insert {
                        it[customerId] = customer.id.value
                        it[postalCode] = address.postalCode
                        it[prefecture] = address.prefecture
                        it[city] = address.city
                        it[streetAddress] = address.streetAddress
                    }
                }
            }
        }
    }

    private fun ResultRow.toCustomer(): Customer {
        return Customer(
            id = CustomerId(this[CustomersTable.id]),
            name = CustomerName(this[CustomersTable.name]),
            email = Email(this[CustomersTable.email]),
            passwordHash = this[CustomersTable.passwordHash],
            status = CustomerStatus.valueOf(this[CustomersTable.status]),
            address = this.getOrNull(AddressesTable.postalCode)?.let { postalCode ->
                Address(
                    postalCode = postalCode,
                    prefecture = this[AddressesTable.prefecture],
                    city = this[AddressesTable.city],
                    streetAddress = this[AddressesTable.streetAddress],
                )
            },
        )
    }
}
