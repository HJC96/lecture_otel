package com.example.paymentservice.model

import java.time.LocalDateTime
import java.util.*

data class Payment(
    val paymentId: String = UUID.randomUUID().toString(),
    val orderId: String,
    val userId: String,
    val amount: Double,
    val paymentMethod: String,
    val status: PaymentStatus = PaymentStatus.PENDING,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val processedAt: LocalDateTime? = null
)

enum class PaymentStatus {
    PENDING, SUCCESS, FAILED, CANCELLED
}

data class PaymentRequest(
    val orderId: String,
    val userId: String,
    val amount: Double,
    val paymentMethod: String = "CREDIT_CARD"
)
