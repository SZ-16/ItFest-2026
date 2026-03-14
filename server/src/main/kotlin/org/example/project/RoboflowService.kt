package org.example.project

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import java.util.Base64

class RoboflowService {
    private val client = HttpClient(CIO)
    private val apiKey = "fjMsSTqPCCSjM39hB4xD"
    private val modelId = "construction-site-kzpwa/1"

    suspend fun analyzeImage(fileBytes: ByteArray): String {
        val encodedImage = Base64.getEncoder().encodeToString(fileBytes)

        val response: HttpResponse = client.post("https://serverless.roboflow.com/$modelId") {
            parameter("api_key", apiKey)
            setBody(encodedImage)
        }

        return response.bodyAsText()
    }
}