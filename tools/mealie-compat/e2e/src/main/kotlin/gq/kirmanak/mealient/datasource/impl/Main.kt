package gq.kirmanak.mealient.datasource.impl

import gq.kirmanak.mealient.datasource.ServerUrlProvider
import gq.kirmanak.mealient.datasource.models.AddRecipeIngredient
import gq.kirmanak.mealient.datasource.models.AddRecipeInstruction
import gq.kirmanak.mealient.datasource.models.AddRecipeSettings
import gq.kirmanak.mealient.datasource.models.CreateApiTokenRequest
import gq.kirmanak.mealient.datasource.models.CreateRecipeRequest
import gq.kirmanak.mealient.datasource.models.CreateShoppingListItemRequest
import gq.kirmanak.mealient.datasource.models.CreateShoppingListRequest
import gq.kirmanak.mealient.datasource.models.ParseRecipeURLRequest
import gq.kirmanak.mealient.datasource.models.UpdateRecipeRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.util.UUID
import javax.inject.Provider
import kotlin.system.exitProcess

private val BASE = System.getProperty("mealie.url") ?: "http://127.0.0.1:9926"
private var token: String? = null
private var passes = 0
private var failures = 0

private fun report(ok: Boolean, name: String, detail: String) {
    if (ok) passes++ else failures++
    println("[${if (ok) "PASS" else "FAIL"}] $name" + if (detail.isNotEmpty()) "  -- $detail" else "")
}

private suspend fun step(name: String, block: suspend () -> String?) {
    try {
        report(true, name, block().orEmpty())
    } catch (e: ResponseException) {
        report(false, name, "HTTP ${e.response.status}")
    } catch (e: Throwable) {
        report(false, name, "${e::class.simpleName}: ${e.message?.take(300)}")
    }
}

private fun require(condition: Boolean, message: String) {
    if (!condition) throw IllegalStateException(message)
}

private val json = Json {
    // Same settings as DataSourceModule.provideJson() in the app
    coerceInputValues = true
    ignoreUnknownKeys = true
    encodeDefaults = true
}

/** Mirrors KtorClientBuilderImpl: OkHttp engine, expectSuccess, content encoding, JSON, bearer auth. */
private fun buildClient(withAuth: Boolean) = HttpClient(OkHttp) {
    expectSuccess = true
    install(ContentEncoding) { gzip(); deflate(); identity() }
    install(ContentNegotiation) { json(json) }
    if (withAuth) {
        install(Auth) {
            bearer {
                loadTokens { token?.let { BearerTokens(accessToken = it, refreshToken = "") } }
                sendWithoutRequest { true }
            }
        }
    }
}

private fun urlProvider() = Provider<ServerUrlProvider> {
    object : ServerUrlProvider {
        override suspend fun getUrl(): String = BASE
    }
}

fun main(): Unit = runBlocking {
    println("Running the app's MealieServiceKtor against $BASE\n")

    val anonymous = MealieServiceKtor(buildClient(withAuth = false), urlProvider())

    step("getVersion") { anonymous.getVersion(BASE).version.also { require(it.isNotBlank(), "empty version") } }
    step("getToken (form login)") {
        token = anonymous.getToken("changeme@example.com", "MyPassword").accessToken
        require(!token.isNullOrBlank(), "no token")
        null
    }
    if (token == null) { println("cannot continue without a token"); exitProcess(1) }

    val authedClient = buildClient(withAuth = true)
    val service = MealieServiceKtor(authedClient, urlProvider())

    var userId = ""
    step("createApiToken") { service.createApiToken(CreateApiTokenRequest("e2e")).token.also { require(it.isNotBlank(), "empty") }.let { null } }
    step("getUserSelfInfo") { userId = service.getUserSelfInfo().id; require(userId.isNotBlank(), "no id"); null }

    // ---- recipes and ingredients
    var slug = ""
    var recipeId = ""
    step("createRecipe") { slug = service.createRecipe(CreateRecipeRequest("E2E Soup")).trim('"'); require(slug == "e2e-soup", "slug=$slug"); slug }
    step("updateRecipe (ingredients, instructions, settings)") {
        val request = UpdateRecipeRequest(
            description = "A soup",
            recipeYield = "4 servings",
            recipeIngredient = listOf(
                AddRecipeIngredient(UUID.randomUUID().toString(), "2 cups water"),
                AddRecipeIngredient(UUID.randomUUID().toString(), "1 tsp salt"),
            ),
            recipeInstructions = listOf(AddRecipeInstruction(UUID.randomUUID().toString(), "Boil the water", emptyList())),
            settings = AddRecipeSettings(disableComments = false, public = true),
        )
        val updated = service.updateRecipe(request, slug)
        recipeId = updated.remoteId
        require(updated.ingredients.size == 2, "ingredients=${updated.ingredients.size}")
        require(updated.instructions.size == 1, "instructions=${updated.instructions.size}")
        "${updated.ingredients.size} ingredients, ${updated.instructions.size} instruction"
    }
    step("getRecipe decodes ingredients (the v1 -> v3 break)") {
        val recipe = service.getRecipe(slug)
        require(recipe.name == "E2E Soup", "name=${recipe.name}")
        require(recipe.recipeYield == "4 servings", "yield=${recipe.recipeYield}")
        require(recipe.ingredients.map { it.note } == listOf("2 cups water", "1 tsp salt"), "notes=${recipe.ingredients.map { it.note }}")
        require(recipe.ingredients.none { it.disableAmount }, "disableAmount set")
        "display=${recipe.ingredients.map { it.display }}"
    }
    step("getRecipeSummary lists it") {
        val items = service.getRecipeSummary(1, 15).items
        require(items.any { it.slug == slug }, "not listed")
        "${items.size} recipe(s)"
    }

    // ---- favorites
    step("addFavoriteRecipe + getFavoriteRecipes (by id)") {
        service.addFavoriteRecipe(userId, slug)
        val favorites = service.getFavoriteRecipes().ratings.filter { it.isFavorite }.map { it.recipeId }
        require(recipeId in favorites, "recipe $recipeId not in $favorites")
        null
    }
    step("removeFavoriteRecipe") {
        service.removeFavoriteRecipe(userId, slug)
        val favorites = service.getFavoriteRecipes().ratings.filter { it.isFavorite }.map { it.recipeId }
        require(recipeId !in favorites, "still a favorite")
        null
    }

    // ---- recipe import by URL: the route must exist and accept the body (no recipe at example.com, so 400 is the right answer)
    run {
        val name = "createRecipeFromURL (route and body accepted)"
        try {
            service.createRecipeFromURL(ParseRecipeURLRequest("https://example.com", includeTags = false))
            report(true, name, "imported something")
        } catch (e: ResponseException) {
            val status = e.response.status.value
            report(status == 400, name, "HTTP $status" + if (status == 400) " (no recipe on that page, as expected)" else " (expected 400)")
        } catch (e: Throwable) {
            report(false, name, "${e::class.simpleName}: ${e.message?.take(200)}")
        }
    }

    // ---- shopping lists (households routes)
    var listId = ""
    var foodId = ""
    var unitId = ""
    step("create a food and a unit (setup, not an app call)") {
        val base = "$BASE/api"
        foodId = authedClient.post("$base/foods") { contentType(ContentType.Application.Json); setBody(buildJsonObject { put("name", "e2e-flour-${UUID.randomUUID()}") }) }.body<JsonObject>()["id"]!!.jsonPrimitive.content
        unitId = authedClient.post("$base/units") { contentType(ContentType.Application.Json); setBody(buildJsonObject { put("name", "e2e-cup-${UUID.randomUUID()}") }) }.body<JsonObject>()["id"]!!.jsonPrimitive.content
        null
    }
    step("createShoppingList + getShoppingLists") {
        service.createShoppingList(CreateShoppingListRequest("E2E list"))
        val lists = service.getShoppingLists(1, 50)
        listId = lists.items.first { it.name == "E2E list" }.id
        "total=${lists.total}"
    }
    step("createShoppingListItem (note item and food item)") {
        service.createShoppingListItem(CreateShoppingListItemRequest(listId, false, 0, false, "Milk", 1.0, null, null))
        service.createShoppingListItem(CreateShoppingListItemRequest(listId, false, 1, true, "", 3.0, foodId, unitId))
        null
    }
    var milkId = ""
    step("getShoppingList decodes items with food and unit") {
        val list = service.getShoppingList(listId)
        require(list.listItems.size == 2, "items=${list.listItems.size}")
        val milk = list.listItems.first { it.note == "Milk" }
        milkId = milk.id
        val flour = list.listItems.first { it.food != null }
        require(flour.food?.id == foodId && flour.unit?.id == unitId && flour.quantity == 3.0, "food item=$flour")
        "items: ${list.listItems.map { it.note.ifBlank { it.food?.name } }}"
    }
    step("check an item off (get JSON, edit, PUT)") {
        val item = service.getShoppingListItem(milkId).jsonObject
        service.updateShoppingListItem(milkId, JsonObject(item + ("checked" to JsonPrimitive(true))))
        val checked = service.getShoppingList(listId).listItems.first { it.id == milkId }.checked
        require(checked, "not checked")
        null
    }
    step("rename the list (get JSON, edit, PUT)") {
        val list = service.getShoppingListJson(listId).jsonObject
        service.updateShoppingList(listId, JsonObject(list + ("name" to JsonPrimitive("Renamed list"))))
        require(service.getShoppingLists(1, 50).items.any { it.id == listId && it.name == "Renamed list" }, "not renamed")
        null
    }
    step("getFoods / getUnits") {
        val foods = service.getFoods(100).items.map { it.name }
        val units = service.getUnits(100).items.map { it.name }
        require(foods.any { it.startsWith("e2e-flour") } && units.any { it.startsWith("e2e-cup") }, "foods=$foods units=$units")
        null
    }
    step("deleteShoppingListItem") {
        service.deleteShoppingListItem(milkId)
        require(service.getShoppingList(listId).listItems.none { it.id == milkId }, "still there")
        null
    }
    step("deleteShoppingList") {
        service.deleteShoppingList(listId)
        require(service.getShoppingLists(1, 50).items.none { it.id == listId }, "still listed")
        null
    }
    step("deleteRecipe") {
        service.deleteRecipe(slug)
        require(service.getRecipeSummary(1, 15).items.none { it.slug == slug }, "still listed")
        null
    }

    println("\n$passes passed, $failures failed")
    exitProcess(if (failures == 0) 0 else 1)
}
