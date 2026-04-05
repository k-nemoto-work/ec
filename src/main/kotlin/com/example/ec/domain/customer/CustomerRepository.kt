package com.example.ec.domain.customer

interface CustomerRepository {
    fun save(customer: Customer)
    fun findById(id: CustomerId): Customer?
    fun findByEmail(email: Email): Customer?
    fun update(customer: Customer)
}
