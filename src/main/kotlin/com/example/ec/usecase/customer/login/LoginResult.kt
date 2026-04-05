package com.example.ec.usecase.customer.login

data class LoginResult(
    val accessToken: String,
    val expiresIn: Long,
)
