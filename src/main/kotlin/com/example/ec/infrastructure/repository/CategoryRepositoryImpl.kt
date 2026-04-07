package com.example.ec.infrastructure.repository

import com.example.ec.domain.product.Category
import com.example.ec.domain.product.CategoryId
import com.example.ec.domain.product.CategoryRepository
import com.example.ec.infrastructure.table.CategoriesTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository

@Repository
class CategoryRepositoryImpl : CategoryRepository {

    override fun findById(id: CategoryId): Category? {
        return transaction {
            CategoriesTable
                .selectAll().where { CategoriesTable.id eq id.value }
                .singleOrNull()
                ?.toCategory()
        }
    }

    override fun findAll(): List<Category> {
        return transaction {
            CategoriesTable
                .selectAll()
                .map { it.toCategory() }
        }
    }

    private fun ResultRow.toCategory(): Category =
        Category(
            id = CategoryId(this[CategoriesTable.id]),
            name = this[CategoriesTable.name],
        )
}
