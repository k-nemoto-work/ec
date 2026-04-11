package com.example.ec.infrastructure.repository

import com.example.ec.domain.customer.Address
import com.example.ec.domain.customer.CustomerId
import com.example.ec.domain.order.Order
import com.example.ec.domain.order.OrderId
import com.example.ec.domain.order.OrderItem
import com.example.ec.domain.order.OrderRepository
import com.example.ec.domain.order.OrderStatus
import com.example.ec.domain.order.Payment
import com.example.ec.domain.order.PaymentMethod
import com.example.ec.domain.order.PaymentStatus
import com.example.ec.domain.order.Shipment
import com.example.ec.domain.order.ShipmentStatus
import com.example.ec.domain.product.Money
import com.example.ec.domain.product.ProductId
import com.example.ec.infrastructure.table.OrderItemsTable
import com.example.ec.infrastructure.table.OrdersTable
import com.example.ec.infrastructure.table.PaymentsTable
import com.example.ec.infrastructure.table.ShipmentsTable
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.upsert
import org.springframework.stereotype.Repository

@Repository
class OrderRepositoryImpl : OrderRepository {

    override fun findById(orderId: OrderId): Order? {
        return transaction {
            val orderRow = OrdersTable
                .selectAll().where { OrdersTable.id eq orderId.value }
                .singleOrNull() ?: return@transaction null

            val items = OrderItemsTable
                .selectAll().where { OrderItemsTable.orderId eq orderId.value }
                .map { row ->
                    OrderItem(
                        productId = ProductId(row[OrderItemsTable.productId]),
                        productNameSnapshot = row[OrderItemsTable.productNameSnapshot],
                        priceSnapshot = Money(row[OrderItemsTable.priceSnapshot]),
                    )
                }

            val paymentRow = PaymentsTable
                .selectAll().where { PaymentsTable.orderId eq orderId.value }
                .single()

            val shipmentRow = ShipmentsTable
                .selectAll().where { ShipmentsTable.orderId eq orderId.value }
                .single()

            Order(
                id = OrderId(orderRow[OrdersTable.id]),
                customerId = CustomerId(orderRow[OrdersTable.customerId]),
                items = items,
                totalAmount = Money(orderRow[OrdersTable.totalAmount]),
                status = OrderStatus.valueOf(orderRow[OrdersTable.status]),
                payment = Payment(
                    method = PaymentMethod.valueOf(paymentRow[PaymentsTable.method]),
                    status = PaymentStatus.valueOf(paymentRow[PaymentsTable.status]),
                ),
                shipment = Shipment(
                    address = Address(
                        postalCode = shipmentRow[ShipmentsTable.postalCode],
                        prefecture = shipmentRow[ShipmentsTable.prefecture],
                        city = shipmentRow[ShipmentsTable.city],
                        streetAddress = shipmentRow[ShipmentsTable.streetAddress],
                    ),
                    status = ShipmentStatus.valueOf(shipmentRow[ShipmentsTable.status]),
                ),
                orderedAt = orderRow[OrdersTable.orderedAt],
            )
        }
    }

    override fun findByCustomerId(customerId: CustomerId, page: Int, size: Int): List<Order> {
        return transaction {
            val orderRows = OrdersTable
                .selectAll().where { OrdersTable.customerId eq customerId.value }
                .orderBy(OrdersTable.orderedAt, SortOrder.DESC)
                .limit(size).offset((page * size).toLong())
                .toList()

            if (orderRows.isEmpty()) return@transaction emptyList()

            val orderIds = orderRows.map { it[OrdersTable.id] }

            val itemsByOrderId = OrderItemsTable
                .selectAll().where { OrderItemsTable.orderId inList orderIds }
                .groupBy { it[OrderItemsTable.orderId] }
                .mapValues { (_, rows) ->
                    rows.map { row ->
                        OrderItem(
                            productId = ProductId(row[OrderItemsTable.productId]),
                            productNameSnapshot = row[OrderItemsTable.productNameSnapshot],
                            priceSnapshot = Money(row[OrderItemsTable.priceSnapshot]),
                        )
                    }
                }

            val paymentsByOrderId = PaymentsTable
                .selectAll().where { PaymentsTable.orderId inList orderIds }
                .associate { row ->
                    row[PaymentsTable.orderId] to Payment(
                        method = PaymentMethod.valueOf(row[PaymentsTable.method]),
                        status = PaymentStatus.valueOf(row[PaymentsTable.status]),
                    )
                }

            val shipmentsByOrderId = ShipmentsTable
                .selectAll().where { ShipmentsTable.orderId inList orderIds }
                .associate { row ->
                    row[ShipmentsTable.orderId] to Shipment(
                        address = Address(
                            postalCode = row[ShipmentsTable.postalCode],
                            prefecture = row[ShipmentsTable.prefecture],
                            city = row[ShipmentsTable.city],
                            streetAddress = row[ShipmentsTable.streetAddress],
                        ),
                        status = ShipmentStatus.valueOf(row[ShipmentsTable.status]),
                    )
                }

            orderRows.map { row ->
                val orderId = row[OrdersTable.id]
                Order(
                    id = OrderId(orderId),
                    customerId = customerId,
                    items = itemsByOrderId[orderId] ?: emptyList(),
                    totalAmount = Money(row[OrdersTable.totalAmount]),
                    status = OrderStatus.valueOf(row[OrdersTable.status]),
                    payment = paymentsByOrderId[orderId]
                        ?: error("payments レコードが見つかりません: orderId=$orderId"),
                    shipment = shipmentsByOrderId[orderId]
                        ?: error("shipments レコードが見つかりません: orderId=$orderId"),
                    orderedAt = row[OrdersTable.orderedAt],
                )
            }
        }
    }

    override fun save(order: Order) {
        transaction {
            val now = Clock.System.now()

            // orders の upsert
            OrdersTable.upsert {
                it[id] = order.id.value
                it[customerId] = order.customerId.value
                it[totalAmount] = order.totalAmount.amount
                it[status] = order.status.name
                it[orderedAt] = order.orderedAt
                it[updatedAt] = now
            }

            // order_items: 全削除→再挿入（注文確定後はアイテムが変わらないためシンプルに実装）
            OrderItemsTable.deleteWhere { orderId eq order.id.value }
            order.items.forEach { item ->
                OrderItemsTable.insert {
                    it[orderId] = order.id.value
                    it[productId] = item.productId.value
                    it[productNameSnapshot] = item.productNameSnapshot
                    it[priceSnapshot] = item.priceSnapshot.amount
                }
            }

            // payments の upsert
            PaymentsTable.upsert {
                it[orderId] = order.id.value
                it[method] = order.payment.method.name
                it[status] = order.payment.status.name
            }

            // shipments の upsert
            ShipmentsTable.upsert {
                it[orderId] = order.id.value
                it[postalCode] = order.shipment.address.postalCode
                it[prefecture] = order.shipment.address.prefecture
                it[city] = order.shipment.address.city
                it[streetAddress] = order.shipment.address.streetAddress
                it[status] = order.shipment.status.name
            }
        }
    }
}
