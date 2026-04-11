package com.example.ec.usecase.cart.add

import java.util.UUID

data class AddToCartCommand(
    val customerId: UUID,
    val productId: UUID,
)
