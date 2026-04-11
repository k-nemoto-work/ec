package com.example.ec.usecase.order.place

import com.example.ec.domain.customer.Address
import com.example.ec.domain.order.PaymentMethod
import java.util.UUID

data class PlaceOrderCommand(
    val customerId: UUID,
    val shippingAddress: Address,
    val paymentMethod: PaymentMethod,
)
