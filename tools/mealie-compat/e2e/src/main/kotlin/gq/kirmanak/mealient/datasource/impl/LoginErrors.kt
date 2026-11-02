package gq.kirmanak.mealient.datasource.impl

import gq.kirmanak.mealient.datasource.ServerUrlProvider
import gq.kirmanak.mealient.datasource.models.ErrorDetail
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import javax.inject.Provider

/** What the login screen shows, using the mapping from MealieDataSourceImpl.authenticate + AuthenticationViewModel. */
private suspend fun shown(service: MealieServiceKtor, user: String, pass: String): String {
    return try {
        service.getToken(user, pass); "LOGGED IN"
    } catch (e: Throwable) {
        val detail = (e as? ResponseException)?.response?.let { runCatching { it.body<ErrorDetail>() }.getOrNull() }
        if (e is ResponseException && detail == null) "<could not read the error body> -> raw message: ${e.message?.take(160)}"
        else if (detail?.detail == "Unauthorized") "\"credentials incorrect\" (the friendly message)"
        else "RAW: ${e.message?.take(190)}"
    }
}

fun main(): Unit = runBlocking {
    val base = System.getProperty("mealie.url") ?: "http://127.0.0.1:9927"
    val client = HttpClient(OkHttp) {
        expectSuccess = true
        install(ContentNegotiation) { json(Json { coerceInputValues = true; ignoreUnknownKeys = true; encodeDefaults = true }) }
    }
    val service = MealieServiceKtor(client, Provider<ServerUrlProvider> { object : ServerUrlProvider { override suspend fun getUrl() = base } })
    println("correct credentials:            ${shown(service, "changeme@example.com", "MyPassword")}")
    println("unknown user:                   ${shown(service, "nobody@example.com", "whatever")}")
    for (i in 1..7) println("wrong password, attempt $i:       ${shown(service, "changeme@example.com", "wrong$i")}")
    println("CORRECT credentials, after the failed attempts: ${shown(service, "changeme@example.com", "MyPassword")}")
}
