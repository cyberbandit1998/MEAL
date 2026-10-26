package gq.kirmanak.mealient.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dagger.hilt.android.AndroidEntryPoint
import gq.kirmanak.mealient.logging.Logger
import gq.kirmanak.mealient.ui.AppTheme
import gq.kirmanak.mealient.ui.ThemeSettings
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var logger: Logger

    private val viewModel by viewModels<MainActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        logger.v { "onCreate() called with: savedInstanceState = $savedInstanceState" }
        splashScreen.setKeepOnScreenCondition {
            // Keep the splash until the theme is known, otherwise the wrong theme flashes
            val state = viewModel.appState.value
            state.forcedRoute == ForcedDestination.Undefined || state.themeSettings == null
        }
        setContent {
            val state by viewModel.appState.collectAsState()
            AppTheme(settings = state.themeSettings ?: ThemeSettings()) {
                MealientApp(viewModel)
            }
        }
    }

}