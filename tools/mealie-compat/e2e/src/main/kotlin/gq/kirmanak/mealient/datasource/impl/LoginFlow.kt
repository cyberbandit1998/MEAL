package gq.kirmanak.mealient.datasource.impl

import gq.kirmanak.mealient.datasource.ServerUrlProvider
import gq.kirmanak.mealient.datasource.models.CreateApiTokenRequest
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.plugin
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import javax.inject.Provider
import kotlin.system.exitProcess

/**
 * Replays AuthRepoImpl.authenticate() on ONE long-lived client, exactly as the app wires it:
 *  - AuthKtorConfiguration: bearer { loadTokens = stored token; refreshTokens logs out on a repeated 401; sendWithoutRequest }
 *  - AuthStorageImpl.setAuthToken: tokenChangeListener.onTokenChange() (clearToken) BEFORE storing the new value
 *  - TokenChangeListenerKtor: clears the token cache of every BearerAuthProvider
 */
private var storedToken: String? = null
private var logouts = 0
private var failures = 0

private fun check(ok: Boolean, what: String) { if (!ok) failures++; println("[${if (ok) "PASS" else "FAIL"}] $what") }

fun main(args: Array<String>): Unit = runBlocking {
    val base = System.getProperty("mealie.url") ?: "http://127.0.0.1:9927"
    val user = System.getProperty("mealie.user") ?: "changeme@example.com"
    val pass = System.getProperty("mealie.pass") ?: "MyPassword"
    val client = HttpClient(OkHttp) {
        expectSuccess = true
        install(ContentEncoding) { gzip(); deflate(); identity() }
        install(ContentNegotiation) { json(Json { coerceInputValues = true; ignoreUnknownKeys = true; encodeDefaults = true }) }
        install(Auth) {
            bearer {
                loadTokens { storedToken?.let { BearerTokens(it, "") } }
                refreshTokens {
                    val newTokens = storedToken?.let { BearerTokens(it, "") }
                    if (newTokens?.accessToken == oldTokens?.accessToken && response.status == HttpStatusCode.Unauthorized) {
                        logouts++; storedToken = null; null
                    } else newTokens
                }
                sendWithoutRequest { true }
            }
        }
    }
    val service = MealieServiceKtor(client, Provider<ServerUrlProvider> { object : ServerUrlProvider { override suspend fun getUrl() = base } })
    fun setAuthToken(t: String?) { // AuthStorageImpl.setAuthToken: notify first, then store
        client.plugin(Auth).providers.filterIsInstance<BearerAuthProvider>().forEach { it.clearToken() }
        storedToken = t
    }

    // The app first checks the server (no token yet), then logs in with the same client
    check(service.getVersion(base).version.isNotBlank(), "1. version check on a client with no token")
    try {
        val loginToken = service.getToken(user, pass).accessToken
        check(loginToken.isNotBlank(), "2. login returns an access token")
        setAuthToken(loginToken)
        val apiToken = service.createApiToken(CreateApiTokenRequest("Mealient")).token
        check(apiToken.isNotBlank(), "3. createApiToken is accepted with the token stored one line earlier")
        setAuthToken(apiToken)
        check(service.getRecipeSummary(1, 15).items.size >= 0, "4. a later call works with the API token")
        check(service.getUserSelfInfo().id.isNotBlank(), "5. getUserSelfInfo works with the API token (not the login token)")
        check(logouts == 0, "6. the app did not log itself out during login (logouts=$logouts)")
    } catch (e: ResponseException) {
        check(false, "login flow failed: HTTP ${e.response.status} (logouts=$logouts)")
    } catch (e: Throwable) {
        check(false, "login flow failed: ${e::class.simpleName}: ${e.message?.take(200)}")
    }
    println(if (failures == 0) "\nLOGIN FLOW OK" else "\n$failures problem(s)")
    exitProcess(if (failures == 0) 0 else 1)
}
