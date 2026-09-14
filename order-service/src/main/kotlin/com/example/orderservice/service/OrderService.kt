package com.example.orderservice.service

import com.example.orderservice.model.CreateOrderRequest
import com.example.orderservice.model.Order
import com.example.orderservice.model.OrderStatus
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class OrderService {
    
    private val orders = ConcurrentHashMap<String, Order>()
    
    fun createOrder(request: CreateOrderRequest): Order {
        // 주문 생성 시뮬레이션 (약간의 지연)
        Thread.sleep(100)
        
        val order = Order(
            userId = request.userId,
            productId = request.productId,
            quantity = request.quantity,
            amount = request.amount,
            status = OrderStatus.PENDING
        )
        
        orders[order.orderId] = order
        
        return order
    }
    
    fun getOrder(orderId: String): Order? {
        return orders[orderId]
    }
    
    fun updateOrderStatus(orderId: String, status: OrderStatus): Order? {
        val order = orders[orderId]
        return if (order != null) {
            val updatedOrder = order.copy(status = status)
            orders[orderId] = updatedOrder
            updatedOrder
        } else {
            null
        }
    }
    
    fun getAllOrders(): List<Order> {
        return orders.values.toList()
    }
}