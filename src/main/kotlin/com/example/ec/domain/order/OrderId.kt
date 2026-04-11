package com.example.ec.domain.order

import java.util.UUID

data class OrderId(val value: UUID) {
    companion object {
        fun generate(): OrderId = OrderId(UUID.randomUUID())
    }
}
