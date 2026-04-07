package com.example.ec.usecase.product.register

import java.util.UUID

data class RegisterProductCommand(
    val name: String,
    val price: Long,
    val description: String,
    val categoryId: UUID,
)
