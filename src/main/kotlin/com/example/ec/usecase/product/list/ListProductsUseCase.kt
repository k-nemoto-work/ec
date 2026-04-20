package com.example.ec.usecase.product.list

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ListProductsUseCase(
    private val productQueryService: ProductQueryService,
) {

    fun execute(query: ListProductsQuery): ProductListResult =
        productQueryService.findPageWithCount(query)
}
