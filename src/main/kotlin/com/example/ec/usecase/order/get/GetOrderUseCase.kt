package com.example.ec.usecase.order.get

import com.example.ec.domain.exception.ResourceNotFoundException
import com.example.ec.domain.exception.UnauthorizedAccessException
import com.example.ec.domain.order.OrderId
import com.example.ec.domain.order.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class GetOrderUseCase(
    private val orderRepository: OrderRepository,
) {

    fun execute(orderId: UUID, customerId: UUID): OrderResult {
        val order = orderRepository.findById(OrderId(orderId))
            ?: throw ResourceNotFoundException("注文", orderId.toString())

        if (order.customerId.value != customerId) {
            throw UnauthorizedAccessException("この注文へのアクセス権限がありません")
        }

        return OrderResult(
            orderId = order.id.value,
            customerId = order.customerId.value,
            items = order.items.map { item ->
                OrderItemResult(
                    productId = item.productId.value,
                    productNameSnapshot = item.productNameSnapshot,
                    priceSnapshot = item.priceSnapshot.amount,
                )
            },
            totalAmount = order.totalAmount.amount,
            status = order.status.name,
            paymentMethod = order.payment.method.name,
            paymentStatus = order.payment.status.name,
            shipmentStatus = order.shipment.status.name,
            shippingPostalCode = order.shipment.address.postalCode,
            shippingPrefecture = order.shipment.address.prefecture,
            shippingCity = order.shipment.address.city,
            shippingStreetAddress = order.shipment.address.streetAddress,
            orderedAt = order.orderedAt.toString(),
        )
    }
}
