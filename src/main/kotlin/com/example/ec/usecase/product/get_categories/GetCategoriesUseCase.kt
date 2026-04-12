package com.example.ec.usecase.product.get_categories

import com.example.ec.domain.product.CategoryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class GetCategoriesUseCase(
    private val categoryRepository: CategoryRepository,
) {

    fun execute(): List<CategoryResult> {
        return categoryRepository.findAll().map { CategoryResult(it.id.value, it.name) }
    }
}
