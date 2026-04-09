package com.example.ec.usecase.favorite.remove

import java.util.UUID

data class RemoveFromFavoriteCommand(
    val customerId: UUID,
    val productId: UUID,
)
