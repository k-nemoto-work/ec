package com.example.ec.domain.product

interface ProductRepository {
    fun save(product: Product)
    fun update(product: Product)
    fun findById(id: ProductId): Product?
    fun findAllOnSale(categoryId: CategoryId?, page: Int, size: Int): List<Product>
    fun countOnSale(categoryId: CategoryId?): Long
}
