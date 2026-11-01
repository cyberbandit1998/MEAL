package gq.kirmanak.mealient.test

import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltTestApplication
import gq.kirmanak.mealient.logging.Logger
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
// Pinned to the SDK the app is compiled against. NEWEST_SDK follows Robolectric, and since 4.16 it is
// Android 16 (API 36), which needs Java 21 to run, while the project builds and tests on Java 17.
@Config(application = HiltTestApplication::class, manifest = Config.NONE, sdk = [34])
abstract class HiltRobolectricTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var logger: Logger

    @Before
    fun inject() {
        hiltRule.inject()
    }
}