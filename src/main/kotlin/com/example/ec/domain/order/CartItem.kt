package com.example.ec.domain.order

import com.example.ec.domain.product.ProductId
import kotlinx.datetime.Instant

data class CartItem(
    val productId: ProductId,
    val addedAt: Instant,
)
