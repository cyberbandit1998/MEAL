package gq.kirmanak.mealient.datasource.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response of `GET /api/users/self/favorites`. Mealie v2 moved favorites into the ratings API,
 * where recipes are identified by their id rather than by their slug.
 */
@Serializable
data class GetFavoritesResponse(
    @SerialName("ratings") val ratings: List<GetUserRatingResponse> = emptyList(),
)

@Serializable
data class GetUserRatingResponse(
    @SerialName("recipeId") val recipeId: String,
    @SerialName("isFavorite") val isFavorite: Boolean = false,
)
