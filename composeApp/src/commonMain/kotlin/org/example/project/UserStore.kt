package org.example.project

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String? = null,
    val username: String,
    val email: String,
    val password: String,
    val special_needs: List<String> = emptyList()
)

object UserStore {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    private fun HttpRequestBuilder.applyHeaders() {
        header("apikey", SupabaseConfig.ANON_KEY)
        header("Authorization", "Bearer ${SupabaseConfig.ANON_KEY}")
        header("Content-Type", "application/json")
    }

    suspend fun login(email: String, password: String): User? {
        return try {
            val response = client.get("${SupabaseConfig.URL}/rest/v1/users") {
                applyHeaders()
                parameter("email", "eq.$email")
                parameter("password", "eq.$password")
                parameter("select", "*")
            }
            val users: List<User> = response.body()
            users.firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun register(
        username: String,
        email: String,
        password: String,
        specialNeeds: List<String> = emptyList()
    ): Boolean {
        return try {
            val response = client.post("${SupabaseConfig.URL}/rest/v1/users") {
                applyHeaders()
                header("Prefer", "return=minimal")
                contentType(ContentType.Application.Json)
                setBody(User(
                    username = username,
                    email = email,
                    password = password,
                    special_needs = specialNeeds
                ))
            }
            println("Register status: ${response.status}")
            response.status == HttpStatusCode.Created
        } catch (e: Exception) {
            println("Register error: ${e.message}")
            false
        }
    }

    suspend fun emailExists(email: String): Boolean {
        return try {
            val response = client.get("${SupabaseConfig.URL}/rest/v1/users") {
                applyHeaders()
                parameter("email", "eq.$email")
                parameter("select", "email")
            }
            val users: List<User> = response.body()
            users.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }
}