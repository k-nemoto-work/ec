package com.example.ec.usecase.customer.update_address

import java.util.UUID

data class UpdateAddressCommand(
    val customerId: UUID,
    val postalCode: String,
    val prefecture: String,
    val city: String,
    val streetAddress: String,
)
