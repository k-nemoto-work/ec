package com.example.ec.usecase.product.get

import com.example.ec.domain.exception.ResourceNotFoundException
import com.example.ec.domain.product.ProductId
import com.example.ec.domain.product.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class GetProductForManagementUseCase(
    private val productRepository: ProductRepository,
) {

    fun execute(productId: UUID): ProductResult {
        val product = productRepository.findById(ProductId(productId))
            ?: throw ResourceNotFoundException("商品", productId.toString())

        return ProductResult(
            productId = product.id.value,
            name = product.name.value,
            price = product.price.amount,
            description = product.description,
            categoryId = product.categoryId.value,
            status = product.status.name,
        )
    }
}
