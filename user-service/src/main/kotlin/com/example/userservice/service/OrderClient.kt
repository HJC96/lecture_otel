package com.example.userservice.service

import com.example.userservice.model.CreateOrderRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
class OrderClient(
    @Value("\${order.service.url:http://order-service:8082}")
    private val orderServiceUrl: String
) {
    
    private val webClient = WebClient.builder()
        .baseUrl(orderServiceUrl)
        .build()
    
    fun createOrder(request: CreateOrderRequest): Mono<Map<String, Any>> {
        return webClient.post()
            .uri("/api/orders")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(object : org.springframework.core.ParameterizedTypeReference<Map<String, Any>>() {})
            .doOnNext { response ->
                println("Order created: ${response["orderId"]}")
            }
            .doOnError { error ->
                println("Order creation failed: ${error.message}")
            }
    }
}