package com.example.ec.usecase.product.update

import java.util.UUID

data class UpdateProductCommand(
    val productId: UUID,
    val name: String,
    val price: Long,
    val description: String,
    val categoryId: UUID,
)
