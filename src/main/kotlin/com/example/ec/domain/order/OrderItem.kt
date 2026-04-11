package com.example.ec.domain.order

import com.example.ec.domain.product.Money
import com.example.ec.domain.product.ProductId

data class OrderItem(
    val productId: ProductId,
    val productNameSnapshot: String,
    val priceSnapshot: Money,
)
