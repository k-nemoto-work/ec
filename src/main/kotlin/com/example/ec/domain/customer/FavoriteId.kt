package com.example.ec.domain.customer

import java.util.UUID

data class FavoriteId(val value: UUID) {
    companion object {
        fun generate(): FavoriteId = FavoriteId(UUID.randomUUID())
    }
}
