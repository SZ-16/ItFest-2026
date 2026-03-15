package org.example.project

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val username: String,
    val password: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)