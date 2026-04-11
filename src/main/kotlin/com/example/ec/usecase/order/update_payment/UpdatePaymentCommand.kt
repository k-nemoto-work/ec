package com.example.ec.usecase.order.update_payment

import java.util.UUID

data class UpdatePaymentCommand(
    val orderId: UUID,
    val customerId: UUID,
)
