package com.example.ec.domain.customer

import java.util.UUID

data class CustomerId(val value: UUID) {
    companion object {
        fun generate(): CustomerId = CustomerId(UUID.randomUUID())
    }
}
