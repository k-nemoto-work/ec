package com.example.ec.domain.order

import java.util.UUID

data class CartId(val value: UUID) {
    companion object {
        fun generate(): CartId = CartId(UUID.randomUUID())
    }
}
