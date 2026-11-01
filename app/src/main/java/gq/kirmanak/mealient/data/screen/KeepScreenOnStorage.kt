package gq.kirmanak.mealient.data.screen

import kotlinx.coroutines.flow.Flow

/**
 * Whether the screen is kept awake while a recipe is open, so that it does not turn off in the
 * middle of cooking. It is on by default, which is how the recipe screen always behaved.
 */
interface KeepScreenOnStorage {

    val isKeepScreenOnFlow: Flow<Boolean>

    suspend fun setKeepScreenOn(keepScreenOn: Boolean)

    companion object {
        const val DEFAULT = true
    }
}
