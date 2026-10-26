package gq.kirmanak.mealient.datasource

import com.google.common.truth.Truth.assertThat
import gq.kirmanak.mealient.datasource.models.GetFavoritesResponse
import gq.kirmanak.mealient.datasource.models.GetFoodsResponse
import gq.kirmanak.mealient.datasource.models.GetRecipeResponse
import gq.kirmanak.mealient.datasource.models.GetRecipesResponse
import gq.kirmanak.mealient.datasource.models.GetShoppingListResponse
import gq.kirmanak.mealient.datasource.models.GetShoppingListsResponse
import gq.kirmanak.mealient.datasource.models.GetTokenResponse
import gq.kirmanak.mealient.datasource.models.GetUnitsResponse
import gq.kirmanak.mealient.datasource.models.GetUserInfoResponse
import gq.kirmanak.mealient.datasource.models.VersionResponse
import kotlinx.serialization.json.Json
import org.junit.Test

/**
 * Decodes responses recorded from a real Mealie 3.29 server, so that the models keep working with
 * the current API. See tools/mealie-compat for how the fixtures were produced.
 */
class MealieV3ResponsesTest {

    // Keep in sync with DataSourceModule.provideJson()
    private val json = Json {
        coerceInputValues = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private fun fixture(name: String): String {
        val stream = checkNotNull(javaClass.classLoader?.getResourceAsStream("mealie-v3/$name.json")) {
            "Fixture $name is missing"
        }
        return stream.bufferedReader().use { it.readText() }
    }

    @Test
    fun `token response is decoded`() {
        val actual = json.decodeFromString<GetTokenResponse>(fixture("token"))
        assertThat(actual.accessToken).isNotEmpty()
    }

    @Test
    fun `version response is decoded`() {
        val actual = json.decodeFromString<VersionResponse>(fixture("about"))
        assertThat(actual.version).isNotEmpty()
    }

    @Test
    fun `user info is decoded without legacy favorites field`() {
        val actual = json.decodeFromString<GetUserInfoResponse>(fixture("user_self"))
        assertThat(actual.id).isNotEmpty()
    }

    @Test
    fun `recipe without legacy amount flags is decoded`() {
        val actual = json.decodeFromString<GetRecipeResponse>(fixture("recipe"))

        assertThat(actual.ingredients).hasSize(3)
        assertThat(actual.ingredients.any { it.disableAmount }).isFalse()
        assertThat(actual.ingredients.any { it.isFood }).isFalse()
        assertThat(actual.settings?.disableAmount).isNotEqualTo(true)
    }

    @Test
    fun `recipe keeps quantity unit and food of a parsed ingredient`() {
        val actual = json.decodeFromString<GetRecipeResponse>(fixture("recipe"))

        val parsed = actual.ingredients.single { it.food != null }
        assertThat(parsed.quantity).isEqualTo(2.0)
        assertThat(parsed.unit?.name).isEqualTo("cup")
        assertThat(parsed.food?.name).isEqualTo("flour")
    }

    @Test
    fun `note only ingredient has no food unit or quantity`() {
        val actual = json.decodeFromString<GetRecipeResponse>(fixture("recipe"))

        val noteOnly = actual.ingredients.first { it.food == null }
        assertThat(noteOnly.unit).isNull()
        assertThat(noteOnly.quantity).isNull()
        assertThat(noteOnly.display).isEqualTo(noteOnly.note)
    }

    @Test
    fun `recipes page is decoded`() {
        val actual = json.decodeFromString<GetRecipesResponse>(fixture("recipes"))
        assertThat(actual.items).isNotEmpty()
    }

    @Test
    fun `favorites are reported by recipe id`() {
        val favorites = json.decodeFromString<GetFavoritesResponse>(fixture("favorites"))
        val recipes = json.decodeFromString<GetRecipesResponse>(fixture("recipes"))

        val favoriteIds = favorites.ratings.filter { it.isFavorite }.map { it.recipeId }
        assertThat(favoriteIds).isNotEmpty()
        assertThat(recipes.items.map { it.remoteId }).containsAtLeastElementsIn(favoriteIds)
    }

    @Test
    fun `shopping lists page is decoded`() {
        val actual = json.decodeFromString<GetShoppingListsResponse>(fixture("shopping_lists"))
        assertThat(actual.items).isNotEmpty()
    }

    @Test
    fun `shopping list with items and recipe reference is decoded`() {
        val actual = json.decodeFromString<GetShoppingListResponse>(fixture("shopping_list"))

        assertThat(actual.listItems).isNotEmpty()
        assertThat(actual.recipeReferences).hasSize(1)
    }

    @Test
    fun `foods are decoded`() {
        val actual = json.decodeFromString<GetFoodsResponse>(fixture("foods"))
        assertThat(actual.items).isNotEmpty()
    }

    @Test
    fun `units are decoded`() {
        val actual = json.decodeFromString<GetUnitsResponse>(fixture("units"))
        assertThat(actual.items).isNotEmpty()
    }
}
