package com.example.calc.infra

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val httpClient = HttpClient(Android) {
    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
}

@Serializable
data class RatesResponse(
    val date: String,
    val rates: Map<String, Double>
)

suspend fun fetchLatestRates(): RatesResponse = httpClient
    .get("https://api.frankfurter.dev/v1/latest")
    .body()