package com.example.userservice.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val status: UserStatus = UserStatus.ACTIVE
)

enum class UserStatus {
    ACTIVE, INACTIVE, SUSPENDED
}

data class CreateOrderRequest(
    val userId: String,
    val productId: String,
    val quantity: Int,
    val amount: Double
)
