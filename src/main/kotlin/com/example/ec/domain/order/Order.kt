package com.example.ec.domain.order

import com.example.ec.domain.customer.Address
import com.example.ec.domain.customer.CustomerId
import com.example.ec.domain.exception.BusinessRuleViolationException
import com.example.ec.domain.product.Money
import com.example.ec.domain.product.Product
import com.example.ec.domain.product.ProductStatus
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

data class Order(
    val id: OrderId,
    val customerId: CustomerId,
    val items: List<OrderItem>,
    val totalAmount: Money,
    val status: OrderStatus,
    val payment: Payment,
    val shipment: Shipment,
    val orderedAt: Instant,
) {
    /**
     * 注文をキャンセルする。
     *
     * PENDING または CONFIRMED 状態の注文のみキャンセル可能。
     *
     * @throws BusinessRuleViolationException SHIPPING/DELIVERED 状態の場合
     */
    fun cancel(): Order {
        if (status == OrderStatus.SHIPPING || status == OrderStatus.DELIVERED) {
            throw BusinessRuleViolationException("配送中または配送完了の注文はキャンセルできません。現在のステータス: $status")
        }
        if (status == OrderStatus.CANCELLED) {
            throw BusinessRuleViolationException("既にキャンセル済みの注文です")
        }
        return copy(status = OrderStatus.CANCELLED)
    }

    /**
     * 決済ステータスを PAID に更新する（モック）。
     *
     * @throws BusinessRuleViolationException 既に PAID の場合
     */
    fun updatePayment(): Order {
        if (payment.status == PaymentStatus.PAID) {
            throw BusinessRuleViolationException("既に決済済みの注文です")
        }
        return copy(payment = payment.copy(status = PaymentStatus.PAID))
    }

    /**
     * 配送ステータスを進める。
     *
     * 決済ステータスが PAID でないと SHIPPED に進められない。
     * DELIVERED への遷移後は変更不可。
     *
     * @throws BusinessRuleViolationException 決済未完了で SHIPPED に進めようとした場合、または不正な遷移の場合
     */
    fun updateShipment(newStatus: ShipmentStatus): Order {
        val currentStatus = shipment.status

        if (newStatus == ShipmentStatus.SHIPPED && payment.status != PaymentStatus.PAID) {
            throw BusinessRuleViolationException("決済が完了していない注文は配送ステータスを SHIPPED に変更できません")
        }

        val allowed = when (currentStatus) {
            ShipmentStatus.NOT_SHIPPED -> newStatus == ShipmentStatus.SHIPPED
            ShipmentStatus.SHIPPED -> newStatus == ShipmentStatus.DELIVERED
            ShipmentStatus.DELIVERED -> false
        }
        if (!allowed) {
            throw BusinessRuleViolationException("${currentStatus} から ${newStatus} への配送ステータス変更は許可されていません")
        }

        val newOrderStatus = if (newStatus == ShipmentStatus.DELIVERED) OrderStatus.DELIVERED else OrderStatus.SHIPPING
        return copy(
            status = newOrderStatus,
            shipment = shipment.copy(status = newStatus),
        )
    }

    companion object {
        /**
         * カートの商品から注文を作成する。
         *
         * @throws BusinessRuleViolationException カートが空の場合、または ON_SALE 以外の商品が含まれる場合
         */
        fun create(
            customerId: CustomerId,
            products: List<Product>,
            shippingAddress: Address,
            paymentMethod: PaymentMethod,
        ): Order {
            if (products.isEmpty()) {
                throw BusinessRuleViolationException("注文する商品が1件以上必要です")
            }

            products.forEach { product ->
                if (product.status != ProductStatus.ON_SALE) {
                    throw BusinessRuleViolationException(
                        "注文できない商品が含まれています: ${product.name.value}（ステータス: ${product.status}）"
                    )
                }
            }

            val items = products.map { product ->
                OrderItem(
                    productId = product.id,
                    productNameSnapshot = product.name.value,
                    priceSnapshot = product.price,
                )
            }

            val totalAmount = Money(items.sumOf { item -> item.priceSnapshot.amount })

            return Order(
                id = OrderId.generate(),
                customerId = customerId,
                items = items,
                totalAmount = totalAmount,
                status = OrderStatus.PENDING,
                payment = Payment(method = paymentMethod, status = PaymentStatus.UNPAID),
                shipment = Shipment(address = shippingAddress, status = ShipmentStatus.NOT_SHIPPED),
                orderedAt = Clock.System.now(),
            )
        }
    }
}
