package com.example.ec.usecase.cart.get

import java.util.UUID

data class CartResult(
    val cartId: UUID?,
    val items: List<CartItemResult>,
    val totalAmount: Long,
)
