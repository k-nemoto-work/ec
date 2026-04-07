package com.example.ec.usecase.product.list

import java.util.UUID

data class ProductSummary(
    val productId: UUID,
    val name: String,
    val price: Long,
    val categoryId: UUID,
    val status: String,
)
