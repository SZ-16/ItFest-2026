package org.example.project

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import java.util.Base64

class RoboflowService {
    private val client = HttpClient(CIO)
    private val apiKey = "fjMsSTqPCCSjM39hB4xD"
    private val modelId = "construction-site-km7bh/1"

    suspend fun analyzeImage(fileBytes: ByteArray): String {
        val encodedImage = Base64.getEncoder().encodeToString(fileBytes)

        val response: HttpResponse = client.post("https://serverless.roboflow.com/$modelId") {
            parameter("api_key", apiKey)
            parameter("confidence", 30)
            parameter("overlap", 25)
            setBody(encodedImage)
        }

        return response.bodyAsText()
    }

    fun extractDetectedClasses(jsonResult: String): List<String> {
        val classes = mutableListOf<String>()
        val regex = Regex(""""class"\s*:\s*"([^"]+)"""")
        regex.findAll(jsonResult).forEach {
            classes.add(it.groupValues[1].lowercase())
        }
        return classes
    }

    fun summarizeDetections(jsonResult: String): String {
        val confidenceRegex = Regex(""""class"\s*:\s*"([^"]+)".*?"confidence"\s*:\s*([\d.]+)""")
        val detections = confidenceRegex.findAll(jsonResult).map {
            val className = it.groupValues[1]
            val confidence = (it.groupValues[2].toDoubleOrNull() ?: 0.0) * 100
            "$className (${"%.0f".format(confidence)}%)"
        }.toList()

        return if (detections.isEmpty()) "Nothing detected"
        else "Detected: ${detections.joinToString(", ")}"
    }
}