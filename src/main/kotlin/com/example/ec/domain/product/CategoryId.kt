package com.example.ec.domain.product

import java.util.UUID

data class CategoryId(val value: UUID) {
    companion object {
        fun generate(): CategoryId = CategoryId(UUID.randomUUID())
    }
}
