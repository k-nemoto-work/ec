package com.example.ec.domain.order

import com.example.ec.domain.customer.CustomerId
import com.example.ec.domain.exception.BusinessRuleViolationException
import com.example.ec.domain.exception.ResourceNotFoundException
import com.example.ec.domain.product.Product
import com.example.ec.domain.product.ProductId
import com.example.ec.domain.product.ProductStatus

data class Cart(
    val id: CartId,
    val customerId: CustomerId,
    val items: List<CartItem>,
) {
    /**
     * 商品をカートに追加する。
     *
     * 「販売中」ステータスの商品のみ追加できる。
     * 同一商品の重複追加は不可。
     *
     * @throws BusinessRuleViolationException 商品が販売中でない場合、または既にカートに存在する場合
     */
    fun addItem(product: Product): Cart {
        if (product.status != ProductStatus.ON_SALE) {
            throw BusinessRuleViolationException("販売中（ON_SALE）の商品のみカートに追加できます。現在のステータス: ${product.status}")
        }
        if (items.any { it.productId == product.id }) {
            throw BusinessRuleViolationException("この商品は既にカートに追加されています: ${product.id.value}")
        }
        return copy(items = items + CartItem(productId = product.id))
    }

    /**
     * 商品をカートから削除する。
     *
     * @throws ResourceNotFoundException 指定した商品がカートに存在しない場合
     */
    fun removeItem(productId: ProductId): Cart {
        if (items.none { it.productId == productId }) {
            throw ResourceNotFoundException("カートアイテム", productId.value.toString())
        }
        return copy(items = items.filter { it.productId != productId })
    }

    companion object {
        fun create(customerId: CustomerId): Cart =
            Cart(
                id = CartId.generate(),
                customerId = customerId,
                items = emptyList(),
            )
    }
}
