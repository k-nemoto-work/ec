package com.example.ec.usecase.product.list

data class ProductListResult(
    val products: List<ProductSummary>,
    val totalCount: Long,
    val page: Int,
    val size: Int,
)
