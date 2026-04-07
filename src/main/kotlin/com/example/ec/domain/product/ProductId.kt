package com.example.ec.domain.product

import java.util.UUID

data class ProductId(val value: UUID) {
    companion object {
        fun generate(): ProductId = ProductId(UUID.randomUUID())
    }
}
