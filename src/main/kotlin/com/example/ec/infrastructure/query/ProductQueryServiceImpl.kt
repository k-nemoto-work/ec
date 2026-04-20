package com.example.ec.infrastructure.query

import com.example.ec.infrastructure.table.ProductsTable
import com.example.ec.usecase.product.list.ListProductsQuery
import com.example.ec.usecase.product.list.ProductListResult
import com.example.ec.usecase.product.list.ProductQueryService
import com.example.ec.usecase.product.list.ProductSummary
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class ProductQueryServiceImpl : ProductQueryService {

    override fun findPageWithCount(query: ListProductsQuery): ProductListResult {
        return transaction {
            val categoryId = query.categoryId

            val baseQuery = ProductsTable.selectAll().where {
                val base = ProductsTable.status eq "ON_SALE"
                if (categoryId != null) base and (ProductsTable.categoryId eq categoryId) else base
            }

            val totalCount = baseQuery.count()

            val products = baseQuery
                .orderBy(ProductsTable.createdAt, SortOrder.DESC)
                .limit(query.size)
                .offset((query.page * query.size).toLong())
                .map { row ->
                    ProductSummary(
                        productId = row[ProductsTable.id],
                        name = row[ProductsTable.name],
                        price = row[ProductsTable.price],
                        categoryId = row[ProductsTable.categoryId],
                        status = row[ProductsTable.status],
                    )
                }

            ProductListResult(products, totalCount, query.page, query.size)
        }
    }
}
