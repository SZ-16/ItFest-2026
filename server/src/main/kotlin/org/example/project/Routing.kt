package org.example.project

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*

fun Application.configureRouting() {
    routing {

        get("/") {
            call.respondText("Accessibility Server is LIVE!")
        }

        route("/api") {

            get("/waypoints") {
                val list = mutableListOf<Waypoint>()
                try {
                    DatabaseConfig.getConnection().use { conn ->
                        val query = "SELECT * FROM accessibility_points"
                        val rs = conn.createStatement().executeQuery(query)
                        while (rs.next()) {
                            list.add(Waypoint(
                                id = rs.getInt("id"),
                                name = rs.getString("name"),
                                category = rs.getString("category"),
                                description = rs.getString("description"),
                                lat = rs.getDouble("lat"),
                                lng = rs.getDouble("lng"),
                                hasRamp = rs.getBoolean("has_ramp"),
                                hasElevator = rs.getBoolean("has_elevator"),
                                isAccessible = rs.getBoolean("is_accessible")
                            ))
                        }
                    }
                    call.respond(list)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Database Error: ${e.message}")
                }
            }

            post("/register") {
                val req = call.receive<RegisterRequest>()
                try {
                    DatabaseConfig.getConnection().use { conn ->
                        val stmt = conn.prepareStatement(
                            "INSERT INTO users (email, username, password_hash) VALUES (?, ?, ?)"
                        )
                        stmt.setString(1, req.email)
                        stmt.setString(2, req.username)
                        stmt.setString(3, req.password)
                        stmt.executeUpdate()

                        call.respondText("""{"success": true, "message": "User registered successfully!"}""", ContentType.Application.Json)
                    }
                } catch (e: Exception) {
                    application.log.error("Register error: ${e.message}")
                    call.respond(HttpStatusCode.Conflict, """{"success": false, "message": "Email already exists or DB error"}""")
                }
            }

            post("/login") {
                val req = call.receive<LoginRequest>()
                try {
                    DatabaseConfig.getConnection().use { conn ->
                        val stmt = conn.prepareStatement(
                            "SELECT id, username FROM users WHERE email = ? AND password_hash = ?"
                        )
                        stmt.setString(1, req.email)
                        stmt.setString(2, req.password)

                        val rs = stmt.executeQuery()
                        if (rs.next()) {
                            val id = rs.getInt("id")
                            val username = rs.getString("username")
                            call.respondText(
                                """{"success": true, "user_id": $id, "username": "$username"}""",
                                ContentType.Application.Json
                            )
                        } else {
                            call.respond(HttpStatusCode.Unauthorized, """{"success": false, "message": "Invalid email or password"}""")
                        }
                    }
                } catch (e: Exception) {
                    application.log.error("Login error: ${e.message}")
                    call.respond(HttpStatusCode.InternalServerError, """{"success": false, "message": "Server error"}""")
                }
            }
        }
    }
}