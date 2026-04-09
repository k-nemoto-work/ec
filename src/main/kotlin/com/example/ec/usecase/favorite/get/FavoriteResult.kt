package com.example.ec.usecase.favorite.get

import java.util.UUID

data class FavoriteResult(
    val favoriteId: UUID?,
    val items: List<FavoriteItemResult>,
)
