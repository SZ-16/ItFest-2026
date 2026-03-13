package org.example.project

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.content.*
import java.io.File

fun Application.configureRouting() {
    val aiService = RoboflowService()

    routing {
        get("/") { call.respondText("Server Organizat Online!") }

        post("/api/upload-frame") {
            val multipartData = call.receiveMultipart()
            var fileName = ""
            var fileBytes: ByteArray? = null

            multipartData.forEachPart { part ->
                if (part is PartData.FileItem) {
                    fileName = "scan_${System.currentTimeMillis()}.jpg"
                    fileBytes = part.streamProvider().readBytes()
                    File("uploads").mkdirs()
                    File("uploads/$fileName").writeBytes(fileBytes!!)
                }
                part.dispose()
            }

            if (fileBytes != null) {
                val jsonResult = aiService.analyzeImage(fileBytes!!)
                val hasAnomaly = jsonResult.contains("predictions") && jsonResult.length > 50

                DatabaseConfig.getConnection().use { conn ->
                    val query = "INSERT INTO site_scans (image_name, has_anomaly, ai_description) VALUES (?, ?, ?)"
                    val stmt = conn.prepareStatement(query)
                    stmt.setString(1, fileName)
                    stmt.setBoolean(2, hasAnomaly)
                    stmt.setString(3, jsonResult)
                    stmt.executeUpdate()
                }
                call.respondText(jsonResult)
            }
        }
    }
}