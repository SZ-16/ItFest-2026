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
            call.respondText("Backend System Online")
        }

        post("/api/upload-frame") {
            val multipartData = call.receiveMultipart()
            var fileName = ""
            var fileBytes: ByteArray? = null

            multipartData.forEachPart { part ->
                if (part is PartData.FileItem) {
                    fileName = "frame_${System.currentTimeMillis()}.jpg"
                    fileBytes = part.streamProvider().readBytes()

                    val dir = File("uploads")
                    if (!dir.exists()) dir.mkdirs()
                    File("uploads/$fileName").writeBytes(fileBytes!!)
                }
                part.dispose()
            }

            if (fileBytes != null) {
                val jsonResult = aiService.analyzeImage(fileBytes!!)

                val constructionClasses = listOf("crane", "excavator", "worker", "truck", "forklift", "person")
                val isActive = constructionClasses.any { jsonResult.contains(it) }

                try {
                    DatabaseConfig.getConnection().use { conn ->
                        val query = "INSERT INTO site_scans (image_name, has_anomaly, ai_description) VALUES (?, ?, ?)"
                        val stmt = conn.prepareStatement(query)
                        stmt.setString(1, fileName)
                        stmt.setBoolean(2, isActive)
                        stmt.setString(3, jsonResult)
                        stmt.executeUpdate()
                    }
                } catch (e: Exception) {
                    println("DB Error: ${e.message}")
                }

                call.respondText(jsonResult, ContentType.Application.Json)
            } else {
                call.respond(HttpStatusCode.BadRequest, "No image found in request")
            }
        }

        get("/api/status") {
            try {
                DatabaseConfig.getConnection().use { conn ->
                    val rs = conn.createStatement().executeQuery(
                        "SELECT has_anomaly, scan_time FROM site_scans ORDER BY scan_time DESC LIMIT 1"
                    )
                    if (rs.next()) {
                        val active = rs.getBoolean("has_anomaly")
                        val time = rs.getTimestamp("scan_time").toString()
                        call.respond(mapOf("active" to active, "last_update" to time))
                    } else {
                        call.respond(mapOf("active" to false, "message" to "No data yet"))
                    }
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.message ?: "Unknown Error")
            }
        }
    }
}