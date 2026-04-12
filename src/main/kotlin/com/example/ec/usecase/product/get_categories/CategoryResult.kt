package com.example.ec.usecase.product.get_categories

import java.util.UUID

data class CategoryResult(
    val id: UUID,
    val name: String,
)
