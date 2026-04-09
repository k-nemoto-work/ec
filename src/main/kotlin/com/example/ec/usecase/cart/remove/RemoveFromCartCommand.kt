package com.example.ec.usecase.cart.remove

import java.util.UUID

data class RemoveFromCartCommand(
    val customerId: UUID,
    val productId: UUID,
)
