package com.example.ec.domain.order

import com.example.ec.domain.customer.Address

data class Shipment(
    val address: Address,
    val status: ShipmentStatus,
)
