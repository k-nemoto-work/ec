package com.example.ec.usecase.product.update

import com.example.ec.domain.exception.ResourceNotFoundException
import com.example.ec.domain.product.*
import com.example.ec.usecase.product.get.ProductResult
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UpdateProductUseCase(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
) {

    fun execute(command: UpdateProductCommand): ProductResult {
        val product = productRepository.findById(ProductId(command.productId))
            ?: throw ResourceNotFoundException("商品", command.productId.toString())

        val categoryId = CategoryId(command.categoryId)
        categoryRepository.findById(categoryId)
            ?: throw ResourceNotFoundException("カテゴリ", command.categoryId.toString())

        val updatedProduct = product.update(
            name = ProductName(command.name),
            price = Money(command.price),
            description = command.description,
            categoryId = categoryId,
        )

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
