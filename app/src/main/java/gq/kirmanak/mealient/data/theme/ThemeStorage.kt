package gq.kirmanak.mealient.data.theme

import gq.kirmanak.mealient.ui.ThemeMode
import gq.kirmanak.mealient.ui.ThemeSettings
import kotlinx.coroutines.flow.Flow

interface ThemeStorage {

    val themeSettingsFlow: Flow<ThemeSettings>

    suspend fun setThemeMode(mode: ThemeMode)

    suspend fun setUseDynamicColor(useDynamicColor: Boolean)
}
