package com.example.ec.usecase.customer.get_profile

import java.util.UUID

data class CustomerProfileResult(
    val id: UUID,
    val name: String,
    val email: String,
    val status: String,
    val address: AddressResult?,
)

data class AddressResult(
    val postalCode: String,
    val prefecture: String,
    val city: String,
    val streetAddress: String,
)
