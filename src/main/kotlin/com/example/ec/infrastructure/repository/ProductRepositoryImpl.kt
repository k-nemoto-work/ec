package com.example.ec.infrastructure.repository

import com.example.ec.domain.product.*
import com.example.ec.infrastructure.table.ProductsTable
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository

@Repository
class ProductRepositoryImpl : ProductRepository {

    override fun save(product: Product) {
        transaction {
            val now = Clock.System.now()
            ProductsTable.insert {
                it[id] = product.id.value
                it[name] = product.name.value
                it[price] = product.price.amount
                it[description] = product.description
                it[categoryId] = product.categoryId.value
                it[status] = product.status.name
                it[createdAt] = now
                it[updatedAt] = now
            }
        }
    }

    override fun update(product: Product) {
        transaction {
            val now = Clock.System.now()
            ProductsTable.update({ ProductsTable.id eq product.id.value }) {
                it[name] = product.name.value
                it[price] = product.price.amount
                it[description] = product.description
                it[categoryId] = product.categoryId.value
                it[status] = product.status.name
                it[updatedAt] = now
            }
        }
    }

    override fun findById(id: ProductId): Product? {
        return transaction {
            ProductsTable
                .selectAll().where { ProductsTable.id eq id.value }
                .singleOrNull()
                ?.toProduct()
        }
    }

    override fun findAllByIds(ids: List<ProductId>): List<Product> {
        return transaction {
            ProductsTable
                .selectAll().where { ProductsTable.id inList ids.map { it.value } }
                .map { it.toProduct() }
        }
    }

    override fun findAllOnSale(categoryId: CategoryId?, page: Int, size: Int): List<Product> {
        return transaction {
            val query = ProductsTable.selectAll().where {
                val statusFilter = ProductsTable.status eq ProductStatus.ON_SALE.name
                if (categoryId != null) {
                    statusFilter and (ProductsTable.categoryId eq categoryId.value)
                } else {
                    statusFilter
                }
            }
            query
                .orderBy(ProductsTable.createdAt, SortOrder.DESC)
                .limit(size).offset((page * size).toLong())
                .map { it.toProduct() }
        }
    }

    override fun countOnSale(categoryId: CategoryId?): Long {
        return transaction {
            ProductsTable.selectAll().where {
                val statusFilter = ProductsTable.status eq ProductStatus.ON_SALE.name
                if (categoryId != null) {
                    statusFilter and (ProductsTable.categoryId eq categoryId.value)
                } else {
                    statusFilter
                }
            }.count()
        }
    }

    private fun ResultRow.toProduct(): Product =
        Product(
            id = ProductId(this[ProductsTable.id]),
            name = ProductName(this[ProductsTable.name]),
            price = Money(this[ProductsTable.price]),
            description = this[ProductsTable.description],
            categoryId = CategoryId(this[ProductsTable.categoryId]),
            status = ProductStatus.valueOf(this[ProductsTable.status]),
        )
}
