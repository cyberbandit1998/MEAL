package gq.kirmanak.mealient.data.theme

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import gq.kirmanak.mealient.data.storage.PreferencesStorage
import gq.kirmanak.mealient.logging.Logger
import gq.kirmanak.mealient.ui.ThemeMode
import gq.kirmanak.mealient.ui.ThemeSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class ThemeStorageImpl @Inject constructor(
    private val preferencesStorage: PreferencesStorage,
    private val logger: Logger,
) : ThemeStorage {

    private val themeModeKey = stringPreferencesKey("themeMode")

    private val useDynamicColorKey = booleanPreferencesKey("useDynamicColor")

    override val themeSettingsFlow: Flow<ThemeSettings>
        get() = combine(
            preferencesStorage.valueUpdates(themeModeKey),
            preferencesStorage.valueUpdates(useDynamicColorKey),
        ) { modeName, useDynamicColor ->
            val default = ThemeSettings()
            ThemeSettings(
                mode = ThemeMode.entries.firstOrNull { it.name == modeName } ?: default.mode,
                useDynamicColor = useDynamicColor ?: default.useDynamicColor,
            )
        }

    override suspend fun setThemeMode(mode: ThemeMode) {
        logger.v { "setThemeMode() called with: mode = $mode" }
        preferencesStorage.storeValues(Pair(themeModeKey, mode.name))
    }

    override suspend fun setUseDynamicColor(useDynamicColor: Boolean) {
        logger.v { "setUseDynamicColor() called with: useDynamicColor = $useDynamicColor" }
        preferencesStorage.storeValues(Pair(useDynamicColorKey, useDynamicColor))
    }
}
