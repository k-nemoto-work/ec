package com.example.ec.usecase.cart.get

import java.util.UUID

data class CartItemResult(
    val productId: UUID,
    val productName: String,
    val price: Long,
    val status: String,
)
