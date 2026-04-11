package com.example.ec.usecase.order.update_shipment

import com.example.ec.domain.order.ShipmentStatus
import java.util.UUID

data class UpdateShipmentCommand(
    val orderId: UUID,
    val customerId: UUID,
    val newStatus: ShipmentStatus,
)
