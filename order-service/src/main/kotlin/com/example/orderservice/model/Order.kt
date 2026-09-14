package com.example.orderservice.model

import java.time.LocalDateTime
import java.util.*

data class Order(
    val orderId: String = UUID.randomUUID().toString(),
    val userId: String,
    val productId: String,
    val quantity: Int,
    val amount: Double,
    val status: OrderStatus = OrderStatus.PENDING,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class OrderStatus {
    PENDING, CONFIRMED, CANCELLED, COMPLETED
}

data class CreateOrderRequest(
    val userId: String,
    val productId: String,
    val quantity: Int,
    val amount: Double
)

data class PaymentRequest(
    val orderId: String,
    val userId: String,
    val amount: Double,
    val paymentMethod: String = "CREDIT_CARD"
)
