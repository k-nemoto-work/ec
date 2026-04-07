package com.example.ec.domain.product

interface CategoryRepository {
    fun findById(id: CategoryId): Category?
    fun findAll(): List<Category>
}
