package com.skylex_chess_clock.chessclock.ui.home

import android.widget.ImageView
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.skylex_chess_clock.chessclock.R
import com.skylex_chess_clock.chessclock.data.UserPreferences
import com.skylex_chess_clock.chessclock.data.UserPreferencesRepo
import com.skylex_chess_clock.chessclock.util.*
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit


import com.skylex_chess_clock.chessclock.databinding.FragmentHomeBinding
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
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
            assertTrue(sut.requireView().findViewById<ImageView>(R.id.refresh_button).isClickable)
        }
    }
}