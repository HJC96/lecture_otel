package com.example.userservice.service

import com.example.userservice.model.User
import com.example.userservice.model.UserStatus
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class UserService {
    
    private val users = ConcurrentHashMap<String, User>().apply {
        put("user1", User("user1", "John Doe", "john@example.com", UserStatus.ACTIVE))
        put("user2", User("user2", "Jane Smith", "jane@example.com", UserStatus.ACTIVE))
        put("user3", User("user3", "Bob Wilson", "bob@example.com", UserStatus.SUSPENDED))
    }
    
    fun getUser(userId: String): User? {
        // 사용자 조회 시뮬레이션 (약간의 지연)
        Thread.sleep(50)
        
        return users[userId]
    }
    
    fun validateUserForOrder(userId: String): Boolean {
        val user = getUser(userId)
        return user != null && user.status == UserStatus.ACTIVE
    }
}