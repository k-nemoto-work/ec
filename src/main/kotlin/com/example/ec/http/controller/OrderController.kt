package com.example.ec.http.controller

import com.example.ec.domain.customer.Address
import com.example.ec.domain.order.PaymentMethod
import com.example.ec.domain.order.ShipmentStatus
import com.example.ec.usecase.order.cancel.CancelOrderUseCase
import com.example.ec.usecase.order.get.GetOrderUseCase
import com.example.ec.usecase.order.get.GetOrdersUseCase
import com.example.ec.usecase.order.get.OrderResult
import com.example.ec.usecase.order.get.OrdersResult
import com.example.ec.usecase.order.place.PlaceOrderCommand
import com.example.ec.usecase.order.place.PlaceOrderResult
import com.example.ec.usecase.order.place.PlaceOrderUseCase
import com.example.ec.usecase.order.update_payment.UpdatePaymentCommand
import com.example.ec.usecase.order.update_payment.UpdatePaymentUseCase
import com.example.ec.usecase.order.update_shipment.UpdateShipmentCommand
import com.example.ec.usecase.order.update_shipment.UpdateShipmentUseCase
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/orders")
class OrderController(
    private val placeOrderUseCase: PlaceOrderUseCase,
    private val getOrdersUseCase: GetOrdersUseCase,
    private val getOrderUseCase: GetOrderUseCase,
    private val cancelOrderUseCase: CancelOrderUseCase,
    private val updatePaymentUseCase: UpdatePaymentUseCase,
    private val updateShipmentUseCase: UpdateShipmentUseCase,
) {

    data class PlaceOrderRequest(
        val paymentMethod: String,
        val shippingPostalCode: String,
        val shippingPrefecture: String,
        val shippingCity: String,
        val shippingStreetAddress: String,
    )

    data class UpdateShipmentRequest(
        val status: String,
    )

    @PostMapping
    fun placeOrder(
        authentication: Authentication,
        @RequestBody request: PlaceOrderRequest,
    ): ResponseEntity<PlaceOrderResult> {
        val customerId = UUID.fromString(authentication.principal as String)
        val result = placeOrderUseCase.execute(
            PlaceOrderCommand(
                customerId = customerId,
                shippingAddress = Address(
                    postalCode = request.shippingPostalCode,
                    prefecture = request.shippingPrefecture,
                    city = request.shippingCity,
                    streetAddress = request.shippingStreetAddress,
                ),
                paymentMethod = PaymentMethod.valueOf(request.paymentMethod),
            )
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(result)
    }

    @GetMapping
    fun getOrders(
        authentication: Authentication,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<OrdersResult> {
        val customerId = UUID.fromString(authentication.principal as String)
        val result = getOrdersUseCase.execute(customerId, page, size)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/{orderId}")
    fun getOrder(
        authentication: Authentication,
        @PathVariable orderId: UUID,
    ): ResponseEntity<OrderResult> {
        val customerId = UUID.fromString(authentication.principal as String)
        val result = getOrderUseCase.execute(orderId, customerId)
        return ResponseEntity.ok(result)
    }

    @PatchMapping("/{orderId}/cancel")
    fun cancelOrder(
        authentication: Authentication,
        @PathVariable orderId: UUID,
    ): ResponseEntity<Void> {
        val customerId = UUID.fromString(authentication.principal as String)
        cancelOrderUseCase.execute(orderId, customerId)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/{orderId}/payment")
    fun updatePayment(
        authentication: Authentication,
        @PathVariable orderId: UUID,
    ): ResponseEntity<Void> {
        val customerId = UUID.fromString(authentication.principal as String)
        updatePaymentUseCase.execute(UpdatePaymentCommand(orderId = orderId, customerId = customerId))
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/{orderId}/shipment")
    fun updateShipment(
        authentication: Authentication,
        @PathVariable orderId: UUID,
        @RequestBody request: UpdateShipmentRequest,
    ): ResponseEntity<Void> {
        val customerId = UUID.fromString(authentication.principal as String)
        updateShipmentUseCase.execute(
            UpdateShipmentCommand(
                orderId = orderId,
                customerId = customerId,
                newStatus = ShipmentStatus.valueOf(request.status),
            )
        )
        return ResponseEntity.noContent().build()
    }
}
