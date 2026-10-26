package gq.kirmanak.mealient.data.theme

import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidTest
import gq.kirmanak.mealient.test.HiltRobolectricTest
import gq.kirmanak.mealient.ui.ThemeMode
import gq.kirmanak.mealient.ui.ThemeSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class ThemeStorageImplTest : HiltRobolectricTest() {

    @Inject
    lateinit var subject: ThemeStorageImpl

    @Test
    fun `when nothing is stored then follows the system with dynamic colors`() = runTest {
        assertThat(subject.themeSettingsFlow.first()).isEqualTo(ThemeSettings())
    }

    @Test
    fun `when mode is stored then it is returned`() = runTest {
        subject.setThemeMode(ThemeMode.Dark)
        assertThat(subject.themeSettingsFlow.first().mode).isEqualTo(ThemeMode.Dark)
    }

    @Test
    fun `when dynamic color is disabled then it is returned`() = runTest {
        subject.setUseDynamicColor(false)
        assertThat(subject.themeSettingsFlow.first().useDynamicColor).isFalse()
    }

    @Test
    fun `when mode changes then dynamic color is kept`() = runTest {
        subject.setUseDynamicColor(false)
        subject.setThemeMode(ThemeMode.Light)
        assertThat(subject.themeSettingsFlow.first())
            .isEqualTo(ThemeSettings(mode = ThemeMode.Light, useDynamicColor = false))
    }
}
