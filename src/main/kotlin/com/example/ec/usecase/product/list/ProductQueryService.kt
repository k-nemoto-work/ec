package com.example.ec.usecase.product.list

interface ProductQueryService {
    fun findPageWithCount(query: ListProductsQuery): ProductListResult
}
