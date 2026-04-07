package com.example.ec.usecase.product.get

import java.util.UUID

data class ProductResult(
    val productId: UUID,
    val name: String,
    val price: Long,
    val description: String,
    val categoryId: UUID,
    val status: String,
)
