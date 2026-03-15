package org.example.project

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*

fun Application.configureRouting() {
    routing {

        // Root path to test if server is even awake
        get("/") {
            call.respondText("Accessibility Server is LIVE!")
        }

        // This handles the path: http://localhost:8080/api/waypoints
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
                    // Log the error to the console so you can see why the DB failed
                    application.log.error("Database error: ${e.message}")
                    call.respond(HttpStatusCode.InternalServerError, "Database Error: ${e.message}")
                }
            }
        }

    }
}