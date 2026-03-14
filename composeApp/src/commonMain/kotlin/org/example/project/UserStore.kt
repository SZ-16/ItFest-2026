package org.example.project

data class User(
    val username: String,
    val email: String,
    val password: String
)

object UserStore {
    private val users = mutableListOf(
        User("admin", "admin@example.com", "admin123"),
        User("test", "test@example.com", "test123")
    )

    fun login(email: String, password: String): User? {
        return users.find { it.email == email && it.password == password }
    }

    fun register(username: String, email: String, password: String): Boolean {
        if (users.any { it.email == email }) return false
        users.add(User(username, email, password))
        return true
    }

    fun emailExists(email: String): Boolean {
        return users.any { it.email == email }
    }
}