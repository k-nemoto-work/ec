package com.example.ec.usecase.product.list

import com.example.ec.domain.product.CategoryId
import com.example.ec.domain.product.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ListProductsUseCase(
    private val productRepository: ProductRepository,
) {

    fun execute(query: ListProductsQuery): ProductListResult {
        val categoryId = query.categoryId?.let { CategoryId(it) }
        val products = productRepository.findAllOnSale(categoryId, query.page, query.size)
        val totalCount = productRepository.countOnSale(categoryId)

        return ProductListResult(
            products = products.map { product ->
                ProductSummary(
                    productId = product.id.value,
                    name = product.name.value,
                    price = product.price.amount,
                    categoryId = product.categoryId.value,
                    status = product.status.name,
                )
            },
            totalCount = totalCount,
            page = query.page,
            size = query.size,
        )
    }
}
