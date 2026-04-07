package com.example.ec.usecase.product.register

import com.example.ec.domain.exception.ResourceNotFoundException
import com.example.ec.domain.product.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class RegisterProductUseCase(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
) {

    fun execute(command: RegisterProductCommand): UUID {
        val categoryId = CategoryId(command.categoryId)
        categoryRepository.findById(categoryId)
            ?: throw ResourceNotFoundException("カテゴリ", command.categoryId.toString())

        val price = Money(command.price)
        val product = Product.create(
            name = ProductName(command.name),
            price = price,
            description = command.description,
            categoryId = categoryId,
        )

        productRepository.save(product)
        return product.id.value
    }
}
