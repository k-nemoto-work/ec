package com.example.ec.usecase.customer.register

data class RegisterCustomerCommand(
    val name: String,
    val email: String,
    val password: String,
)
