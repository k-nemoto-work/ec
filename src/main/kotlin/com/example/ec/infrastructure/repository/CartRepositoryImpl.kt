package com.example.ec.infrastructure.repository

import com.example.ec.domain.customer.CustomerId
import com.example.ec.domain.order.Cart
import com.example.ec.domain.order.CartId
import com.example.ec.domain.order.CartItem
import com.example.ec.domain.order.CartRepository
import com.example.ec.domain.product.ProductId
import com.example.ec.infrastructure.table.CartItemsTable
import com.example.ec.infrastructure.table.CartsTable
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository

@Repository
class CartRepositoryImpl : CartRepository {

    override fun findByCustomerId(customerId: CustomerId): Cart? {
        return transaction {
            val cartRow = CartsTable
                .selectAll().where { CartsTable.customerId eq customerId.value }
                .singleOrNull() ?: return@transaction null

            val cartId = cartRow[CartsTable.id]
            val items = CartItemsTable
                .selectAll().where { CartItemsTable.cartId eq cartId }
                .orderBy(CartItemsTable.addedAt, SortOrder.ASC)
                .map { row ->
                    CartItem(
                        productId = ProductId(row[CartItemsTable.productId]),
                        addedAt = row[CartItemsTable.addedAt],
                    )
                }

            Cart(
                id = CartId(cartId),
                customerId = customerId,
                items = items,
            )
        }
    }

    override fun save(cart: Cart) {
        transaction {
            // 同一顧客の同時リクエストによる競合を避けるため insertIgnore を使用
            CartsTable.insertIgnore {
                it[id] = cart.id.value
                it[customerId] = cart.customerId.value
            }

            // 差分更新: 現在のDBアイテムと比較して追加・削除のみ実行
            val currentProductIds = CartItemsTable
                .selectAll().where { CartItemsTable.cartId eq cart.id.value }
                .map { it[CartItemsTable.productId] }
                .toSet()

            val desiredProductIds = cart.items.map { it.productId.value }.toSet()

            val toDelete = currentProductIds - desiredProductIds
            if (toDelete.isNotEmpty()) {
                CartItemsTable.deleteWhere {
                    (cartId eq cart.id.value) and (productId inList toDelete.toList())
                }
            }

            val toInsert = cart.items.filter { it.productId.value !in currentProductIds }
            toInsert.forEach { item ->
                CartItemsTable.insert {
                    it[cartId] = cart.id.value
                    it[productId] = item.productId.value
                    it[addedAt] = item.addedAt
                }
            }
        }
    }
}
