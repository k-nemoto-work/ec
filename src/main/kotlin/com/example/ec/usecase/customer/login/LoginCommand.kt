package com.example.ec.usecase.customer.login

data class LoginCommand(
    val email: String,
    val password: String,
)
