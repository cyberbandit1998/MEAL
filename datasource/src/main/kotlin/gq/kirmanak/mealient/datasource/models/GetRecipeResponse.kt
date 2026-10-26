package gq.kirmanak.mealient.datasource.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetRecipeResponse(
    @SerialName("id") val remoteId: String,
    @SerialName("name") val name: String,
    @SerialName("recipeYield") val recipeYield: String = "",
    @SerialName("recipeIngredient") val ingredients: List<GetRecipeIngredientResponse> = emptyList(),
    @SerialName("recipeInstructions") val instructions: List<GetRecipeInstructionResponse> = emptyList(),
    @SerialName("settings") val settings: GetRecipeSettingsResponse? = null,
)

@Serializable
data class GetRecipeSettingsResponse(
    // Mealie v2 removed this setting, so it must not be required.
    @SerialName("disableAmount") val disableAmount: Boolean = false,
)

@Serializable
data class GetRecipeIngredientResponse(
    @SerialName("note") val note: String = "",
    @SerialName("unit") val unit: GetUnitResponse? = null,
    @SerialName("food") val food: GetFoodResponse? = null,
    @SerialName("quantity") val quantity: Double? = null,
    @SerialName("display") val display: String,
    @SerialName("referenceId") val referenceId: String,
    @SerialName("title") val title: String? = null,
    // Mealie v2 removed both of the flags below, so they must not be required.
    @SerialName("isFood") val isFood: Boolean = false,
    @SerialName("disableAmount") val disableAmount: Boolean = false,
)

@Serializable
data class GetRecipeInstructionResponse(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String = "",
    @SerialName("text") val text: String,
    @SerialName("ingredientReferences") val ingredientReferences: List<GetRecipeInstructionIngredientReference> = emptyList(),
)

@Serializable
data class GetRecipeInstructionIngredientReference(
    @SerialName("referenceId") val referenceId: String,
)