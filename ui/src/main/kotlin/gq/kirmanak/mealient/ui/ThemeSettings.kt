package gq.kirmanak.mealient.ui

enum class ThemeMode {
    /** Follow the system light/dark setting. */
    System,
    Light,
    Dark,
}

/**
 * User's appearance preferences.
 *
 * @property useDynamicColor use the wallpaper based colors (Material You) where the device
 * supports them, otherwise the app's own palette is used.
 */
data class ThemeSettings(
    val mode: ThemeMode = ThemeMode.System,
    val useDynamicColor: Boolean = true,
)
