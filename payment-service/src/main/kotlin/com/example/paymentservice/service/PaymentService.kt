package com.example.paymentservice.service

import com.example.paymentservice.model.Payment
import com.example.paymentservice.model.PaymentRequest
import com.example.paymentservice.model.PaymentStatus
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

@Service
class PaymentService {
    
    private val payments = ConcurrentHashMap<String, Payment>()
    
    fun processPayment(request: PaymentRequest): Payment {
        // 결제 처리 시뮬레이션 (약간의 지연)
        Thread.sleep(200)
        
        val payment = Payment(
            orderId = request.orderId,
            userId = request.userId,
            amount = request.amount,
            paymentMethod = request.paymentMethod,
            status = PaymentStatus.PENDING
        )
        
        // 결제 처리 로직 시뮬레이션
        val processedPayment = simulatePaymentProcessing(payment)
        
        payments[processedPayment.paymentId] = processedPayment
        
        return processedPayment
    }
    
    private fun simulatePaymentProcessing(payment: Payment): Payment {
        // 90% 성공률로 결제 처리 시뮬레이션
        Thread.sleep(50)
        
        val isSuccess = Random.nextDouble() < 0.9
        val status = if (isSuccess) PaymentStatus.SUCCESS else PaymentStatus.FAILED
        
        return payment.copy(
            status = status,
            processedAt = LocalDateTime.now()
        )
    }
    
    fun getPayment(paymentId: String): Payment? {
        return payments[paymentId]
    }
    
    fun getPaymentsByOrderId(orderId: String): List<Payment> {
        return payments.values.filter { it.orderId == orderId }
    }
    
    fun getAllPayments(): List<Payment> {
        return payments.values.toList()
    }
}