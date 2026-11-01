package gq.kirmanak.mealient.data.screen

import androidx.datastore.preferences.core.booleanPreferencesKey
import gq.kirmanak.mealient.data.storage.PreferencesStorage
import gq.kirmanak.mealient.logging.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class KeepScreenOnStorageImpl @Inject constructor(
    private val preferencesStorage: PreferencesStorage,
    private val logger: Logger,
) : KeepScreenOnStorage {

    private val keepScreenOnKey = booleanPreferencesKey("keepScreenOnInRecipe")

    override val isKeepScreenOnFlow: Flow<Boolean>
        get() = preferencesStorage.valueUpdates(keepScreenOnKey)
            .map { it ?: KeepScreenOnStorage.DEFAULT }

    override suspend fun setKeepScreenOn(keepScreenOn: Boolean) {
        logger.v { "setKeepScreenOn() called with: keepScreenOn = $keepScreenOn" }
        preferencesStorage.storeValues(Pair(keepScreenOnKey, keepScreenOn))
    }
}
