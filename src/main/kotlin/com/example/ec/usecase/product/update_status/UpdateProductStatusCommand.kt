package com.example.ec.usecase.product.update_status

import java.util.UUID

data class UpdateProductStatusCommand(
    val productId: UUID,
    val status: String,
)
