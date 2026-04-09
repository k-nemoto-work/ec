package com.example.ec.usecase.favorite.add

import java.util.UUID

data class AddToFavoriteCommand(
    val customerId: UUID,
    val productId: UUID,
)
