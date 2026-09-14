package com.example.orderservice.service

import com.example.orderservice.model.PaymentRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
class PaymentClient(
    @Value("\${payment.service.url:http://payment-service:8083}")
    private val paymentServiceUrl: String
) {
    
    private val webClient = WebClient.builder()
        .baseUrl(paymentServiceUrl)
        .build()
    
    fun processPayment(request: PaymentRequest): Mono<Map<String, Any>> {
        return webClient.post()
            .uri("/api/payments")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(object : org.springframework.core.ParameterizedTypeReference<Map<String, Any>>() {})
            .doOnNext { response ->
                println("Payment processed: ${response["paymentId"]}")
            }
            .doOnError { error ->
                println("Payment failed: ${error.message}")
            }
    }
}