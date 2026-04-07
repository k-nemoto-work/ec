package com.example.ec.usecase.product.update_status

import com.example.ec.domain.exception.DomainValidationException
import com.example.ec.domain.exception.ResourceNotFoundException
import com.example.ec.domain.product.ProductId
import com.example.ec.domain.product.ProductRepository
import com.example.ec.domain.product.ProductStatus
import com.example.ec.usecase.product.get.ProductResult
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UpdateProductStatusUseCase(
    private val productRepository: ProductRepository,
) {

    fun execute(command: UpdateProductStatusCommand): ProductResult {
        val product = productRepository.findById(ProductId(command.productId))
            ?: throw ResourceNotFoundException("商品", command.productId.toString())

        val newStatus = try {
            ProductStatus.valueOf(command.status)
        } catch (e: IllegalArgumentException) {
            throw DomainValidationException("無効なステータスです: ${command.status}")
        }

        val updatedProduct = product.changeStatus(newStatus)
        productRepository.update(updatedProduct)

        return ProductResult(
            productId = updatedProduct.id.value,
            name = updatedProduct.name.value,
            price = updatedProduct.price.amount,
            description = updatedProduct.description,
            categoryId = updatedProduct.categoryId.value,
            status = updatedProduct.status.name,
        )
    }
}
