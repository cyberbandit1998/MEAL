package gq.kirmanak.mealient.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.google.android.material.color.DynamicColors

/**
 * Applies the user's [ThemeSettings].
 */
@Composable
fun AppTheme(
    settings: ThemeSettings,
    content: @Composable () -> Unit
) {
    val isDarkTheme = when (settings.mode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }

    AppTheme(
        isDarkTheme = isDarkTheme,
        isDynamicColor = settings.useDynamicColor && DynamicColors.isDynamicColorAvailable(),
        content = content,
    )
}

@Composable
fun AppTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    isDynamicColor: Boolean = DynamicColors.isDynamicColorAvailable(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        isDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        isDarkTheme -> DarkColorScheme

        else -> LightColorScheme
    }

    SystemBarsAppearance(isDarkTheme = isDarkTheme)

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

/**
 * Keeps the status and navigation bar icons readable. The app's theme can differ from the
 * system's one, so the icons have to follow the app's theme and not the system's.
 */
@Composable
private fun SystemBarsAppearance(isDarkTheme: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return

    SideEffect {
        val window = view.context.findActivity()?.window ?: return@SideEffect
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !isDarkTheme
            isAppearanceLightNavigationBars = !isDarkTheme
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

object Dimens {

    val Small = 8.dp

    val Intermediate = 12.dp

    val Medium = 16.dp

    val Large = 24.dp

}
