package com.skylex_chess_clock.chessclock.ui.home

import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.test.core.view.MotionEventBuilder
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.skylex_chess_clock.chessclock.R
import com.skylex_chess_clock.chessclock.data.UserPreferences
import com.skylex_chess_clock.chessclock.data.UserPreferencesRepo
import com.skylex_chess_clock.chessclock.ui.settings.SettingsBottomSheetFragment
import com.skylex_chess_clock.chessclock.util.*
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit


import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.TestScheduler
import junit.framework.TestCase.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(application = HiltTestApplication::class)
class HomeFragmentTest: RobolectricFragmentScenarioTestCase<HomeFragment>(HomeFragment::class.java) {

    @get:Rule
    val hiltAndroidRule = HiltAndroidRule(this)

    @get:Rule
    val liveDataRule = InstantTaskExecutorRule()

    @get:Rule
    val rxJavaRule = RxImmediateSchedulerRule()

    private val sut = HomeFragment()


    private val mockUserPreferencesRepo = mockk<UserPreferencesRepo>()

    private val defaultPlayerTime = TimeHelper(5, TimeUnit.MINUTES)
    private val defaultTimeIncrement = TimeHelper(2, TimeUnit.SECONDS)
    private val defaultClockMode = TopLevelFiles.Companion.ClockMode.SUDDEN_DEATH.name

    private val defaultUserPreferences = UserPreferences(
        defaultPlayerTime,
        defaultTimeIncrement,
        TopLevelFiles.Companion.ClockMode.valueOf(defaultClockMode)
    )

    @Before
    fun setup() {
        hiltAndroidRule.inject()
    }

    @Test
    fun `Given fragment, when fragment created, views should be appropriately inflated and populated`() = runTest {
        every { mockUserPreferencesRepo.userPreferencesFlow } returns flowOf(defaultUserPreferences)

        launch(sut) {
            val view = sut.requireView()

            assertFalse(view.findViewById<ImageView>(R.id.play_button).isClickable)
            assertTrue(view.findViewById<ImageView>(R.id.refresh_button).isClickable)
            assertTrue(view.findViewById<ImageView>(R.id.settings_button).isClickable)

            val expectedDisplayTime = TimeUtil.getTimeTextFromSeconds(defaultPlayerTime.toSeconds())

            assertEquals(expectedDisplayTime, view.findViewById<TextView>(R.id.player_one_time).text.toString())
            assertEquals(expectedDisplayTime, view.findViewById<TextView>(R.id.player_two_time).text.toString())

            view.assertVisible(R.id.clock_activity_dimmer)
            view.assertVisible(R.id.play_button)
            view.assertNotVisible(R.id.pause_button)
        }
    }

    @Test
    fun `Given fragment, when player one time is clicked, then player two time should countdown and be activated`() {
        every { mockUserPreferencesRepo.userPreferencesFlow } returns flowOf(defaultUserPreferences)
        val testScheduler = TestScheduler()
        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }

        launch(sut) {
            val view = sut.requireView()

            // Start player two time
            view.findViewById<View>(R.id.player_one_section).dispatchTouchEvent(MotionEventBuilder.newBuilder().setAction(MotionEvent.ACTION_DOWN).build())

            // Assert views change and are displayed properly
            view.assertNotVisible(R.id.clock_activity_dimmer)
            view.assertVisible(R.id.pause_button)

            assertFalse(view.findViewById<View>(R.id.refresh_button).isClickable)
            assertFalse(view.findViewById<View>(R.id.settings_button).isClickable)

            val currentPlayerTwoBackgroundResId = Shadows.shadowOf(view.findViewById<ConstraintLayout>(R.id.player_two_section).background).createdFromResId
            assertEquals(R.color.color_primary, currentPlayerTwoBackgroundResId)


            // Assert time counts down
            val advanceTimeSeconds = 10L
            testScheduler.advanceTimeBy(advanceTimeSeconds, TimeUnit.SECONDS)
            val expectedTimeOnPlayerTwoClock = TimeUtil.getTimeTextFromSeconds(defaultPlayerTime.toSeconds() - advanceTimeSeconds)

            assertEquals(expectedTimeOnPlayerTwoClock, view.findViewById<TextView>(R.id.player_two_time).text.toString())
        }
    }

    @Test
    fun `Given fragment, when player two time is clicked, then other player should countdown and be activated`() {
        every { mockUserPreferencesRepo.userPreferencesFlow } returns flowOf(defaultUserPreferences)
        val testScheduler = TestScheduler()
        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }

        launch(sut) {
            val view = sut.requireView()

            // Start player two time
            view.findViewById<View>(R.id.player_two_section).dispatchTouchEvent(MotionEventBuilder.newBuilder().setAction(MotionEvent.ACTION_DOWN).build())

            // Assert views change and are displayed properly
            view.assertNotVisible(R.id.clock_activity_dimmer)
            view.assertVisible(R.id.pause_button)

            assertFalse(view.findViewById<View>(R.id.refresh_button).isClickable)
            assertFalse(view.findViewById<View>(R.id.settings_button).isClickable)

            val currentPlayerTwoBackgroundResId = Shadows.shadowOf(view.findViewById<ConstraintLayout>(R.id.player_one_section).background).createdFromResId
            assertEquals(R.color.color_primary, currentPlayerTwoBackgroundResId)


            // Assert time counts down
            val advanceTimeSeconds = 10L
            testScheduler.advanceTimeBy(advanceTimeSeconds, TimeUnit.SECONDS)
            val expectedTimeOnPlayerTwoClock = TimeUtil.getTimeTextFromSeconds(defaultPlayerTime.toSeconds() - advanceTimeSeconds)

            assertEquals(expectedTimeOnPlayerTwoClock, view.findViewById<TextView>(R.id.player_one_time).text.toString())
        }
    }

    @Test
    fun `Given fragment, when player one time is clicked and clock is paused, views should render appropriately`() {
        every { mockUserPreferencesRepo.userPreferencesFlow } returns flowOf(defaultUserPreferences)
        val testScheduler = TestScheduler()
        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }

        launch(sut) {
            val view = sut.requireView()

            // Start player two time
            view.findViewById<View>(R.id.player_one_section).dispatchTouchEvent(MotionEventBuilder.newBuilder().setAction(MotionEvent.ACTION_DOWN).build())

            // Advance time
            var advanceTimeSeconds = 10L
            testScheduler.advanceTimeBy(advanceTimeSeconds, TimeUnit.SECONDS)

            // Pause game
            view.findViewById<View>(R.id.pause_button).performClick()

            // Assert views are displayed appropriately
            view.assertNotVisible(R.id.pause_button)
            view.assertVisible(R.id.play_button)
            view.assertVisible(R.id.clock_activity_dimmer)

            assertTrue(view.findViewById<View>(R.id.refresh_button).isClickable)
            assertTrue(view.findViewById<View>(R.id.settings_button).isClickable)

            // Advance time indefinitely while game is paused
            testScheduler.advanceTimeBy(500, TimeUnit.SECONDS)
            val expectedTimeOnPlayerTwoClock = TimeUtil.getTimeTextFromSeconds(defaultPlayerTime.toSeconds() - advanceTimeSeconds)

            assertEquals(expectedTimeOnPlayerTwoClock, view.findViewById<TextView>(R.id.player_two_time).text.toString())
        }
    }

    @Test
    fun `Given fragment, when game has advanced and reset clock clicked, views should render appropriately`() {
        every { mockUserPreferencesRepo.userPreferencesFlow } returns flowOf(defaultUserPreferences)
        val testScheduler = TestScheduler()
        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }

        launch(sut) {
            val view = sut.requireView()

            // Start player two time
            view.findViewById<View>(R.id.player_one_section).dispatchTouchEvent(MotionEventBuilder.newBuilder().setAction(MotionEvent.ACTION_DOWN).build())

            // Advance time
            val advanceTimeSeconds = 10L
            testScheduler.advanceTimeBy(advanceTimeSeconds, TimeUnit.SECONDS)

            // Pause game
            view.findViewById<View>(R.id.pause_button).performClick()

            // Reset clocks
            view.findViewById<View>(R.id.refresh_button).performClick()

            // Assert views are displayed appropriately
            assertTrue(view.findViewById<ImageView>(R.id.refresh_button).isClickable)
            assertTrue(view.findViewById<ImageView>(R.id.settings_button).isClickable)

            val expectedDisplayTime = TimeUtil.getTimeTextFromSeconds(defaultPlayerTime.toSeconds())

            assertEquals(expectedDisplayTime, view.findViewById<TextView>(R.id.player_one_time).text.toString())
            assertEquals(expectedDisplayTime, view.findViewById<TextView>(R.id.player_two_time).text.toString())

            view.assertVisible(R.id.clock_activity_dimmer)
            view.assertVisible(R.id.play_button)
            view.assertNotVisible(R.id.pause_button)
        }
    }

    private fun View.assertVisible(id: Int) {
        assertTrue(this.findViewById<View>(id).isVisible)
    }

    private fun View.assertNotVisible(id: Int) {
        assertFalse(this.findViewById<View>(id).isVisible)
    }
}