package org.example.project

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.content.*
import io.ktor.http.*
import java.io.File

fun Application.configureRouting() {
    val aiService = RoboflowService()

    routing {

        get("/") {
            call.respondText("Backend Online")
        }

        // ─── UPLOAD PHOTO + LOCATION ───────────────────────────────────────
        post("/api/upload-frame") {
            val multipartData = call.receiveMultipart()

            var fileName = ""
            var fileBytes: ByteArray? = null
            var latitude: Double? = null
            var longitude: Double? = null
            var userId: Int? = null

            multipartData.forEachPart { part ->                when (part) {
                    is PartData.FileItem -> {
                        fileName = "frame_${System.currentTimeMillis()}.jpg"
                        fileBytes = part.streamProvider().readBytes()
                        val dir = File("uploads")
                        if (!dir.exists()) dir.mkdirs()
                        File("uploads/$fileName").writeBytes(fileBytes!!)
                    }
                    is PartData.FormItem -> {
                        when (part.name) {
                            "latitude" -> latitude = part.value.toDoubleOrNull()
                            "longitude" -> longitude = part.value.toDoubleOrNull()
                            "user_id" -> userId = part.value.toIntOrNull()
                        }
                    }
                    else -> {}
                }
                part.dispose()
            }

            if (fileBytes == null) {
                call.respond(HttpStatusCode.BadRequest, "No image found in request")
                return@post
            }

            // Run Roboflow AI analysis
            val jsonResult = aiService.analyzeImage(fileBytes!!)
            val detectedClasses = aiService.extractDetectedClasses(jsonResult)

            // Detect hazard type using new model's classes
            val hazardType = when {
                detectedClasses.any { it in listOf("crane", "excavator", "bulldozer", "machinery", "truck", "loader") } -> "heavy_machinery"
                detectedClasses.any { it in listOf("worker", "person", "helmet", "vest", "construction-worker") } -> "workers_present"
                detectedClasses.any { it in listOf("barrier", "fence", "cone", "sign", "barricade", "debris") } -> "blocked_path"
                detectedClasses.isNotEmpty() -> "construction_activity"
                else -> "unknown"
            }

            val isActive = hazardType != "unknown"

            // Map hazard to disability relevance with more detail
            val affectsWheelchair  = hazardType in listOf("heavy_machinery", "blocked_path", "construction_activity")
            val affectsHearing     = hazardType in listOf("heavy_machinery", "workers_present", "construction_activity")
            val affectsVision      = hazardType in listOf("heavy_machinery", "blocked_path", "workers_present", "construction_activity")
            val affectsAutism      = hazardType in listOf("heavy_machinery", "workers_present", "construction_activity") // High noise/activity
            val affectsChronicPain = hazardType in listOf("blocked_path", "construction_activity") // Potential detours

            try {
                DatabaseConfig.getConnection().use { conn ->
                    val query = """
                        INSERT INTO site_scans (
                            user_id, image_name, has_anomaly, ai_description,
                            latitude, longitude, hazard_type,
                            affects_wheelchair, affects_hearing, affects_vision,
                            affects_autism, affects_chronic_pain
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """.trimIndent()

                    val stmt = conn.prepareStatement(query)
                    stmt.setObject(1, userId)
                    stmt.setString(2, fileName)
                    stmt.setBoolean(3, isActive)
                    stmt.setString(4, jsonResult)
                    stmt.setObject(5, latitude)
                    stmt.setObject(6, longitude)
                    stmt.setString(7, hazardType)
                    stmt.setBoolean(8, affectsWheelchair)
                    stmt.setBoolean(9, affectsHearing)
                    stmt.setBoolean(10, affectsVision)
                    stmt.setBoolean(11, affectsAutism)
                    stmt.setBoolean(12, affectsChronicPain)
                    stmt.executeUpdate()
                }
            } catch (e: Exception) {
                println("DB Error: ${e.message}")
            }

            // Return the full JSON result for more detail
            call.respondText(jsonResult, ContentType.Application.Json)
        }

        // ─── GET ALL HAZARDS FOR MAP ────────────────────────────────────────
        get("/api/hazards") {
            try {
                DatabaseConfig.getConnection().use { conn ->
                    val rs = conn.createStatement().executeQuery("""
                        SELECT id, latitude, longitude, hazard_type, has_anomaly, ai_description,
                               affects_wheelchair, affects_hearing, affects_vision,
                               affects_autism, affects_chronic_pain, scan_time
                        FROM site_scans
                        WHERE has_anomaly = TRUE
                          AND latitude IS NOT NULL
                          AND longitude IS NOT NULL
                        ORDER BY scan_time DESC
                    """.trimIndent())

                    val hazards = buildString {
                        append("[")
                        var first = true
                        while (rs.next()) {
                            if (!first) append(",")
                            val aiDesc = rs.getString("ai_description")?.replace("\"", "\\\"")?.replace("\n", " ") ?: ""
                            append("""
                                {
                                  "id": ${rs.getInt("id")},
                                  "latitude": ${rs.getDouble("latitude")},
                                  "longitude": ${rs.getDouble("longitude")},
                                  "hazard_type": "${rs.getString("hazard_type")}",
                                  "ai_description": "$aiDesc",
                                  "affects_wheelchair": ${rs.getBoolean("affects_wheelchair")},
                                  "affects_hearing": ${rs.getBoolean("affects_hearing")},
                                  "affects_vision": ${rs.getBoolean("affects_vision")},
                                  "affects_autism": ${rs.getBoolean("affects_autism")},
                                  "affects_chronic_pain": ${rs.getBoolean("affects_chronic_pain")},
                                  "scan_time": "${rs.getTimestamp("scan_time")}"
                                }
                            """.trimIndent())
                            first = false
                        }
                        append("]")
                    }

                    call.respondText(hazards, ContentType.Application.Json)
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.message ?: "Unknown Error")
            }
        }

        // ─── GET HAZARDS FILTERED BY DISABILITY ────────────────────────────
        get("/api/hazards/filter") {
            val wheelchair   = call.request.queryParameters["wheelchair"]?.toBoolean() ?: false
            val hearing      = call.request.queryParameters["hearing"]?.toBoolean() ?: false
            val vision       = call.request.queryParameters["vision"]?.toBoolean() ?: false
            val autism       = call.request.queryParameters["autism"]?.toBoolean() ?: false
            val chronicPain  = call.request.queryParameters["chronic_pain"]?.toBoolean() ?: false

            try {
                DatabaseConfig.getConnection().use { conn ->
                    val conditions = mutableListOf("has_anomaly = TRUE", "latitude IS NOT NULL", "longitude IS NOT NULL")
                    if (wheelchair)  conditions.add("affects_wheelchair = TRUE")
                    if (hearing)     conditions.add("affects_hearing = TRUE")
                    if (vision)      conditions.add("affects_vision = TRUE")
                    if (autism)      conditions.add("affects_autism = TRUE")
                    if (chronicPain) conditions.add("affects_chronic_pain = TRUE")

                    val where = conditions.joinToString(" AND ")
                    val rs = conn.createStatement().executeQuery("""
                        SELECT id, latitude, longitude, hazard_type, ai_description,
                               affects_wheelchair, affects_hearing, affects_vision,
                               affects_autism, affects_chronic_pain, scan_time
                        FROM site_scans
                        WHERE $where
                        ORDER BY scan_time DESC
                    """.trimIndent())

                    val hazards = buildString {
                        append("[")
                        var first = true
                        while (rs.next()) {
                            if (!first) append(",")
                            val aiDesc = rs.getString("ai_description")?.replace("\"", "\\\"")?.replace("\n", " ") ?: ""
                            append("""
                                {
                                  "id": ${rs.getInt("id")},
                                  "latitude": ${rs.getDouble("latitude")},
                                  "longitude": ${rs.getDouble("longitude")},
                                  "hazard_type": "${rs.getString("hazard_type")}",
                                  "ai_description": "$aiDesc",
                                  "affects_wheelchair": ${rs.getBoolean("affects_wheelchair")},
                                  "affects_hearing": ${rs.getBoolean("affects_hearing")},
                                  "affects_vision": ${rs.getBoolean("affects_vision")},
                                  "affects_autism": ${rs.getBoolean("affects_autism")},
                                  "affects_chronic_pain": ${rs.getBoolean("affects_chronic_pain")},
                                  "scan_time": "${rs.getTimestamp("scan_time")}"
                                }
                            """.trimIndent())
                            first = false
                        }
                        append("]")
                    }

                    call.respondText(hazards, ContentType.Application.Json)
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.message ?: "Unknown Error")
            }
        }

        // ─── REGISTER USER ──────────────────────────────────────────────────
        post("/api/register") {
            val params = call.receiveParameters()
            val email    = params["email"]    ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing email")
            val password = params["password"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing password")
            val username = params["username"] ?: ""

            try {
                DatabaseConfig.getConnection().use { conn ->
                    val stmt = conn.prepareStatement(
                        "INSERT INTO users (email, password_hash, username) VALUES (?, ?, ?)"
                    )
                    stmt.setString(1, email)
                    stmt.setString(2, password)
                    stmt.setString(3, username)
                    stmt.executeUpdate()
                    call.respondText("""{"success": true}""", ContentType.Application.Json)
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, "Email already exists")
            }
        }

        // ─── LOGIN USER ─────────────────────────────────────────────────────
        post("/api/login") {
            val params   = call.receiveParameters()
            val email    = params["email"]    ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing email")
            val password = params["password"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing password")

            try {
                DatabaseConfig.getConnection().use { conn ->
                    val stmt = conn.prepareStatement(
                        "SELECT id, username FROM users WHERE email = ? AND password_hash = ?"
                    )
                    stmt.setString(1, email)
                    stmt.setString(2, password)
                    val rs = stmt.executeQuery()
                    if (rs.next()) {
                        val id       = rs.getInt("id")
                        val username = rs.getString("username")
                        call.respondText(
                            """{"success": true, "user_id": $id, "username": "$username"}""",
                            ContentType.Application.Json
                        )
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
                    }
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.message ?: "Unknown Error")
            }
        }

        // ─── LATEST STATUS ──────────────────────────────────────────────────
        get("/api/status") {
            try {
                DatabaseConfig.getConnection().use { conn ->
                    val rs = conn.createStatement().executeQuery(
                        "SELECT has_anomaly, scan_time FROM site_scans ORDER BY scan_time DESC LIMIT 1"
                    )
                    if (rs.next()) {
                        val active = rs.getBoolean("has_anomaly")
                        val time   = rs.getTimestamp("scan_time").toString()
                        call.respondText(
                            """{"active": $active, "last_update": "$time"}""",
                            ContentType.Application.Json
                        )
                    } else {
                        call.respondText(
                            """{"active": false, "message": "No data yet"}""",
                            ContentType.Application.Json
                        )
                    }
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.message ?: "Unknown Error")
            }
        }
    }
}
