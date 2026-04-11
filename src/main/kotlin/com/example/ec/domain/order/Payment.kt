package com.example.ec.domain.order

data class Payment(
    val method: PaymentMethod,
    val status: PaymentStatus,
)
