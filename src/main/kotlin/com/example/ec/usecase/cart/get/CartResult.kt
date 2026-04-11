package com.example.ec.usecase.cart.get

data class CartResult(
    val items: List<CartItemResult>,
    val totalAmount: Long,
)
