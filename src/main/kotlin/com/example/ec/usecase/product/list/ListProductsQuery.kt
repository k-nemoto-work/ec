package com.example.ec.usecase.product.list

import java.util.UUID

data class ListProductsQuery(
    val categoryId: UUID? = null,
    val page: Int = 0,
    val size: Int = 20,
)
