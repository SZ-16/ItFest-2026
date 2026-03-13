package org.example.project

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.http.content.*
import java.io.File

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    routing {
        get("/") {
            call.respondText("Serverul merge!")
        }

        post("/api/upload-frame") {
            val multipartData = call.receiveMultipart()
            var fileName = ""

            multipartData.forEachPart { part ->
                when (part) {
                    is PartData.FileItem -> {
                        fileName = part.originalFileName ?: "imagine_santier.jpg"
                        val fileBytes = part.streamProvider().readBytes()

                        val uploadDir = File("uploads")
                        if (!uploadDir.exists()) uploadDir.mkdir()

                        File("uploads/$fileName").writeBytes(fileBytes)
                        println("✅ Am primit și salvat poza: $fileName")
                    }
                    else -> {}
                }
                part.dispose()
            }

            call.respondText("Poza $fileName a ajuns cu succes pe server!")
        }
    }
}