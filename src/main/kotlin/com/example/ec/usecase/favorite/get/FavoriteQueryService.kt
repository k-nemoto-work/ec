package com.example.ec.usecase.favorite.get

import java.util.UUID

interface FavoriteQueryService {
    fun findByCustomerId(customerId: UUID): FavoriteResult
}
