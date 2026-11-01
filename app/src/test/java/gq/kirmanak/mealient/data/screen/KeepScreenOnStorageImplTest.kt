package gq.kirmanak.mealient.data.screen

import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidTest
import gq.kirmanak.mealient.test.HiltRobolectricTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class KeepScreenOnStorageImplTest : HiltRobolectricTest() {

    @Inject
    lateinit var subject: KeepScreenOnStorageImpl

    @Test
    fun `when nothing is stored then the screen is kept on like before the setting existed`() = runTest {
        assertThat(subject.isKeepScreenOnFlow.first()).isTrue()
    }

    @Test
    fun `when turned off then it is off`() = runTest {
        subject.setKeepScreenOn(false)
        assertThat(subject.isKeepScreenOnFlow.first()).isFalse()
    }

    @Test
    fun `when turned off and on again then it is on`() = runTest {
        subject.setKeepScreenOn(false)
        subject.setKeepScreenOn(true)
        assertThat(subject.isKeepScreenOnFlow.first()).isTrue()
    }
}
