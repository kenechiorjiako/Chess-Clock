package com.skylex_chess_clock.chessclock.ui.settings

import android.os.Bundle
import android.os.Looper
import android.widget.TextView
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.skylex_chess_clock.chessclock.*
import com.skylex_chess_clock.chessclock.util.*
import com.skylex_chess_clock.chessclock.util.TopLevelFiles.Companion.ClockMode
import junit.framework.TestCase.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.parceler.Parcels
import org.robolectric.Shadows
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class SettingsBottomSheetFragmentTest: RobolectricActivityTestScenario<SingleFragmentActivity>(SingleFragmentActivity::class.java) {

    val sut = SettingsBottomSheetFragment()

    private val defaultPlayerTime = TimeMapper(5, TimeUnit.MINUTES)
    private val defaultTimeIncrement = TimeMapper(2, TimeUnit.SECONDS)
    private val defaultClockMode = ClockMode.SUDDEN_DEATH.name

    @Before
    fun setup() {
        val outBundle = Bundle()

        outBundle.putSerializable(CLOCK_MODE_RESULT_KEY, ClockMode.valueOf(defaultClockMode))
        outBundle.putParcelable(TIME_RESULT_KEY, defaultPlayerTime)
        outBundle.putParcelable(INCREMENT_RESULT_KEY, defaultTimeIncrement)

        sut.arguments = outBundle
    }

    @Test
    fun `Given bottom sheet shown for SUDDEN DEATH clock mode, then sheet should be displayed properly`() {
        launch().onActivity {
            sut.show(it.supportFragmentManager, sut.tag)

            Shadows.shadowOf(Looper.getMainLooper()).idle()
            assertEquals(defaultClockMode, sut.requireView().findViewById<TextView>(R.id.clock_text_view).text.toString())
            assertEquals(defaultPlayerTime.toString(), sut.requireView().findViewById<TextView>(R.id.time_text_view).text.toString())
            assertEquals(defaultTimeIncrement.toString(), sut.requireView().findViewById<TextView>(R.id.increment_text_view).text.toString())

            sut.requireView().assertNotVisible(R.id.increment_text_view)
            sut.requireView().assertNotVisible(R.id.increment_text_view_label)
        }
    }

    @Test
    fun `Given bottom sheet shown for INCREMENT clock mode, then sheet should be displayed properly`() {
        launch().onActivity {
            val outBundle = Bundle()

            outBundle.putSerializable(CLOCK_MODE_RESULT_KEY, ClockMode.valueOf(ClockMode.INCREMENT.name))
            outBundle.putParcelable(TIME_RESULT_KEY, defaultPlayerTime)
            outBundle.putParcelable(INCREMENT_RESULT_KEY, defaultTimeIncrement)

            sut.arguments = outBundle

            sut.show(it.supportFragmentManager, sut.tag)

            Shadows.shadowOf(Looper.getMainLooper()).idle()
            assertEquals(ClockMode.INCREMENT.name, sut.requireView().findViewById<TextView>(R.id.clock_text_view).text.toString())
            assertEquals(defaultPlayerTime.toString(), sut.requireView().findViewById<TextView>(R.id.time_text_view).text.toString())
            assertEquals(defaultTimeIncrement.toString(), sut.requireView().findViewById<TextView>(R.id.increment_text_view).text.toString())

            sut.requireView().assertVisible(R.id.increment_text_view)
            sut.requireView().assertVisible(R.id.increment_text_view_label)
        }
    }

    @Test
    fun `Given bottom sheet, when action view clicked, then bottom sheet should dismiss and fragment result sent`() {
        launch().onActivity {
            it.supportFragmentManager.setFragmentResultListener(SettingsBottomSheetFragment.RESULT_KEY, it) {_, result ->
                assertEquals(defaultPlayerTime, Parcels.unwrap(result.getParcelable(TIME_RESULT_KEY)))
                assertEquals(defaultTimeIncrement, Parcels.unwrap(result.getParcelable(INCREMENT_RESULT_KEY)))
                assertEquals(ClockMode.valueOf(defaultClockMode), result.getSerializable(CLOCK_MODE_RESULT_KEY) as ClockMode)
            }

            sut.show(it.supportFragmentManager, sut.tag)

            Shadows.shadowOf(Looper.getMainLooper()).idle()

            sut.requireView().performClick(R.id.action_button)
        }
    }


}