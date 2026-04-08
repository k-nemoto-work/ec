package com.example.ec.domain.customer

import com.example.ec.domain.product.ProductId
import kotlinx.datetime.Instant

data class FavoriteItem(
    val productId: ProductId,
    val addedAt: Instant,
)
