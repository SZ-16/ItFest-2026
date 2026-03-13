package org.example.project

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.http.content.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import java.io.File
import java.sql.DriverManager

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    routing {

        get("/") {
            call.respondText("Serverul de monitorizare șantier este activ!")
        }

        post("/api/upload-frame") {
            val multipartData = call.receiveMultipart()
            var fileName = ""
            var fileBytes: ByteArray? = null

            multipartData.forEachPart { part ->
                if (part is PartData.FileItem) {
                    fileName = "frame_${System.currentTimeMillis()}.jpg"
                    fileBytes = part.streamProvider().readBytes()

                    val uploadDir = File("uploads")
                    if (!uploadDir.exists()) uploadDir.mkdir()
                    File("uploads/$fileName").writeBytes(fileBytes!!)
                }
                part.dispose()
            }

            if (fileBytes != null) {
                val client = HttpClient(CIO)

                val apiKey = "PUNE_AICI_API_KEY_ROBOFLOW"
                val modelId = "NUME_MODEL/VERSIUNE"

                val response: HttpResponse = client.post("https://detect.roboflow.com/$modelId") {
                    parameter("api_key", apiKey)
                    setBody(fileBytes!!)
                }

                val jsonResponse = response.bodyAsText()
                println("🔍 Rezultat YOLO: $jsonResponse")

                val hasAnomaly = jsonResponse.contains("predictions") && jsonResponse.length > 50

                try {
                    val dbUrl = "jdbc:postgresql://localhost:5432/postgres"
                    val dbUser = "postgres"
                    val dbPass = "PAROLA_TA_PGADMIN"

                    val conn = DriverManager.getConnection(dbUrl, dbUser, dbPass)
                    val query = "INSERT INTO site_scans (image_name, has_anomaly, ai_description) VALUES (?, ?, ?)"
                    val stmt = conn.prepareStatement(query)

                    stmt.setString(1, fileName)
                    stmt.setBoolean(2, hasAnomaly)
                    stmt.setString(3, jsonResponse)

                    stmt.executeUpdate()
                    conn.close()
                    println("Date salvate în PostgreSQL pentru fișierul: $fileName")
                } catch (e: Exception) {
                    println(" Eroare PostgreSQL: ${e.message}")
                }

                call.respondText(jsonResponse)
                client.close()
            } else {
                call.respondText("Eroare: Nu s-a primit nicio imagine.", status = io.ktor.http.HttpStatusCode.BadRequest)
            }
        }
    }
}