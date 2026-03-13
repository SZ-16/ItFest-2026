package org.example.project

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

class RoboflowService {
    private val client = HttpClient(CIO)
    private val apiKey = "fjMsStqPCCSjM39hB4xD"
    private val modelId = "construction-site-kzpwa/1"

    suspend fun analyzeImage(fileBytes: ByteArray): String {
        val response: HttpResponse = client.post("https://detect.roboflow.com/$modelId") {
            parameter("api_key", apiKey)
            setBody(fileBytes)
        }
        return response.bodyAsText()
    }
}