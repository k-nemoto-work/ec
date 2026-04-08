package com.example.ec.usecase.favorite.get

import java.util.UUID

data class FavoriteItemResult(
    val productId: UUID,
    val productName: String,
    val price: Long,
    val status: String,
    val addedAt: String,
)
