package com.example.orderservice.controller

import com.example.orderservice.model.CreateOrderRequest
import com.example.orderservice.model.Order
import com.example.orderservice.model.OrderStatus
import com.example.orderservice.model.PaymentRequest
import com.example.orderservice.service.OrderService
import com.example.orderservice.service.PaymentClient
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderService: OrderService,
    private val paymentClient: PaymentClient
) {
    
    @GetMapping("/{orderId}")
    fun getOrder(@PathVariable orderId: String): ResponseEntity<Order> {
        val order = orderService.getOrder(orderId)
        return if (order != null) {
            ResponseEntity.ok(order)
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    @PostMapping
    fun createOrder(@RequestBody request: CreateOrderRequest): Mono<ResponseEntity<Map<String, Any?>>> {
        return try {
            val order = orderService.createOrder(request)
            
            val paymentRequest = PaymentRequest(
                orderId = order.orderId,
                userId = order.userId,
                amount = order.amount,
                paymentMethod = "CREDIT_CARD"
            )
            
            paymentClient.processPayment(paymentRequest)
                .map { paymentResponse ->
                    val updatedOrder = orderService.updateOrderStatus(order.orderId, OrderStatus.CONFIRMED)
                    
                    val response: Map<String, Any?> = mapOf(
                        "orderId" to order.orderId,
                        "status" to updatedOrder?.status?.name,
                        "paymentId" to paymentResponse["paymentId"],
                        "amount" to order.amount
                    )
                    
                    ResponseEntity.ok(response)
                }
                .onErrorReturn(
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(mapOf("error" to "Payment processing failed"))
                )
        } catch (e: Exception) {
            Mono.just(
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(mapOf("error" to e.message))
            )
        }
    }
    
    @GetMapping
    fun getAllOrders(): ResponseEntity<List<Order>> {
        val orders = orderService.getAllOrders()
        return ResponseEntity.ok(orders)
    }
}