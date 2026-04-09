package com.example.ec.infrastructure.repository

import com.example.ec.domain.customer.CustomerId
import com.example.ec.domain.order.Cart
import com.example.ec.domain.order.CartId
import com.example.ec.domain.order.CartItem
import com.example.ec.domain.order.CartRepository
import com.example.ec.domain.product.ProductId
import com.example.ec.infrastructure.table.CartItemsTable
import com.example.ec.infrastructure.table.CartsTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
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
                .map { row ->
                    CartItem(productId = ProductId(row[CartItemsTable.productId]))
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
            // カートレコードが未存在の場合のみ INSERT
            val exists = CartsTable
                .selectAll().where { CartsTable.id eq cart.id.value }
                .count() > 0

            if (!exists) {
                CartsTable.insert {
                    it[id] = cart.id.value
                    it[customerId] = cart.customerId.value
                }
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
                }
            }
        }
    }
}
