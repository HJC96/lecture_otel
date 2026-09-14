package com.example.userservice.controller

import com.example.userservice.model.CreateOrderRequest
import com.example.userservice.model.User
import com.example.userservice.service.OrderClient
import com.example.userservice.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService,
    private val orderClient: OrderClient
) {
    
    @GetMapping("/{userId}")
    fun getUser(@PathVariable userId: String): ResponseEntity<User> {
        val user = userService.getUser(userId)
        return if (user != null) {
            ResponseEntity.ok(user)
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    @PostMapping("/{userId}/orders")
    fun createOrder(
        @PathVariable userId: String,
        @RequestBody orderRequest: CreateOrderRequest
    ): Mono<ResponseEntity<Map<String, Any>>> {
        // 사용자 유효성 검증
        if (!userService.validateUserForOrder(userId)) {
            return Mono.just(
                ResponseEntity.badRequest()
                    .body(mapOf("error" to "User not found or inactive", "userId" to userId))
            )
        }
        
        // Order Service 호출
        return orderClient.createOrder(orderRequest)
            .map { response ->
                ResponseEntity.status(HttpStatus.CREATED).body(response)
            }
            .onErrorReturn(
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(mapOf("error" to "Failed to create order"))
            )
    }
    
}