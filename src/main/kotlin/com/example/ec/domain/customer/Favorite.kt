package com.example.ec.domain.customer

import com.example.ec.domain.exception.BusinessRuleViolationException
import com.example.ec.domain.exception.ResourceNotFoundException
import com.example.ec.domain.product.ProductId
import com.example.ec.domain.product.ProductStatus
import kotlinx.datetime.Clock

data class Favorite(
    val id: FavoriteId,
    val customerId: CustomerId,
    val items: List<FavoriteItem>,
) {
    /**
     * 商品をお気に入りに追加する。
     * 追加可能なステータス: ON_SALE, RESERVED のみ。
     * 同じ商品の重複追加は不可。
     */
    fun addItem(productId: ProductId, productStatus: ProductStatus): Favorite {
        val allowedStatuses = setOf(ProductStatus.ON_SALE, ProductStatus.RESERVED)
        if (productStatus !in allowedStatuses) {
            throw BusinessRuleViolationException("ステータスが ${productStatus} の商品はお気に入りに追加できません")
        }
        if (items.any { it.productId == productId }) {
            throw BusinessRuleViolationException("この商品は既にお気に入りに追加されています")
        }
        val newItem = FavoriteItem(productId = productId, addedAt = Clock.System.now())
        return copy(items = items + newItem)
    }

    /**
     * 商品をお気に入りから削除する。
     */
    fun removeItem(productId: ProductId): Favorite {
        if (items.none { it.productId == productId }) {
            throw ResourceNotFoundException("お気に入りアイテム", productId.value.toString())
        }
        return copy(items = items.filter { it.productId != productId })
    }

    companion object {
        fun create(customerId: CustomerId): Favorite =
            Favorite(
                id = FavoriteId.generate(),
                customerId = customerId,
                items = emptyList(),
            )
    }
}
