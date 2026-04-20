package com.example.ec.infrastructure.query

import com.example.ec.infrastructure.table.OrderItemsTable
import com.example.ec.infrastructure.table.OrdersTable
import com.example.ec.usecase.order.get.OrderQueryService
import com.example.ec.usecase.order.get.OrderSummaryResult
import com.example.ec.usecase.order.get.OrdersResult
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class OrderQueryServiceImpl : OrderQueryService {

    override fun findOrderSummariesByCustomer(customerId: UUID, page: Int, size: Int): OrdersResult {
        return transaction {
            val totalCount = OrdersTable.selectAll()
                .where { OrdersTable.customerId eq customerId }
                .count()

            val orders = OrdersTable
                .join(OrderItemsTable, JoinType.LEFT, OrdersTable.id, OrderItemsTable.orderId)
                .select(
                    OrdersTable.id,
                    OrdersTable.totalAmount,
                    OrdersTable.status,
                    OrdersTable.orderedAt,
                    OrderItemsTable.productId.count(),
                )
                .where { OrdersTable.customerId eq customerId }
                .groupBy(OrdersTable.id, OrdersTable.totalAmount, OrdersTable.status, OrdersTable.orderedAt)
                .orderBy(OrdersTable.orderedAt, SortOrder.DESC)
                .limit(size)
                .offset((page * size).toLong())
                .map { row ->
                    OrderSummaryResult(
                        orderId = row[OrdersTable.id],
                        totalAmount = row[OrdersTable.totalAmount],
                        status = row[OrdersTable.status],
                        itemCount = row[OrderItemsTable.productId.count()].toInt(),
                        orderedAt = row[OrdersTable.orderedAt].toString(),
                    )
                }

            OrdersResult(orders, totalCount, page, size)
        }
    }
}
