package com.example.paymentservice.controller

import com.example.paymentservice.model.Payment
import com.example.paymentservice.model.PaymentRequest
import com.example.paymentservice.service.PaymentService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/payments")
class PaymentController(
    private val paymentService: PaymentService
) {
    
    @GetMapping("/{paymentId}")
    fun getPayment(@PathVariable paymentId: String): ResponseEntity<Payment> {
        val payment = paymentService.getPayment(paymentId)
        return if (payment != null) {
            ResponseEntity.ok(payment)
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    @PostMapping
    fun processPayment(@RequestBody request: PaymentRequest): ResponseEntity<*> {
        return try {
            val payment = paymentService.processPayment(request)
            
            val responseStatus = if (payment.status.name == "SUCCESS") {
                HttpStatus.CREATED
            } else {
                HttpStatus.BAD_REQUEST
            }
            
            ResponseEntity.status(responseStatus).body(
                mapOf(
                    "paymentId" to payment.paymentId,
                    "orderId" to payment.orderId,
                    "status" to payment.status.name,
                    "amount" to payment.amount,
                    "processedAt" to payment.processedAt?.toString()
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Payment processing failed", "message" to e.message))
        }
    }
    
}