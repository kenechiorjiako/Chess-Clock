package com.skylex_chess_clock.chessclock.ui.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.SavedStateHandle
import com.skylex_chess_clock.chessclock.ui.home.HomeFragmentVM.Companion.NO_PLAYER
import com.skylex_chess_clock.chessclock.ui.home.HomeFragmentVM.Companion.PLAYER_ONE
import com.skylex_chess_clock.chessclock.ui.home.HomeFragmentVM.Companion.PLAYER_TWO
import com.skylex_chess_clock.chessclock.ui.home.HomeFragmentVM.Event
import com.skylex_chess_clock.chessclock.ui.home.HomeFragmentVM.ViewEffect
import com.skylex_chess_clock.chessclock.util.*
import com.skylex_chess_clock.chessclock.util.TopLevelFiles.Companion.ClockMode
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.*
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.TimeUnit

class HomeFragmentVMTest {

    @get:Rule
    val liveDataRule = InstantTaskExecutorRule()

    @get:Rule
    val schedulers = RxImmediateSchedulerRule()

    private lateinit var sut: HomeFragmentVM

    private val mockDataStore = mockk<DataStore<Preferences>>()
    private val mockSavedStateHandle = mockk<SavedStateHandle>()
    private val mockPreferences = mockk<Preferences>()

    private val defaultPlayerTime = TimeHelper(5, TimeUnit.MINUTES)
    private val defaultTimeIncrement = TimeHelper(2, TimeUnit.SECONDS)
    private val defaultClockMode = ClockMode.SUDDEN_DEATH.name

    @Before
    fun setup() {
        every { mockDataStore.data } returns flowOf(mockPreferences)
        every { mockPreferences[PreferenceKeys.timeHelperPreferenceKey] } returns TimeHelper.TimeHelperPreferencesConverter.serialize(defaultPlayerTime)
        every { mockPreferences[PreferenceKeys.timeIncrementPreferenceKey] } returns TimeHelper.TimeHelperPreferencesConverter.serialize(defaultTimeIncrement)
        every { mockPreferences[PreferenceKeys.clockModePreferenceKey] } returns defaultClockMode

        sut = HomeFragmentVM(
            mockSavedStateHandle,
            mockDataStore
        )
    }

    @Test
    fun `Given new viewModel, then data should be set appropriately`() {

        val viewState = sut.viewStates().getOrAwaitValue()

        assertFalse(viewState.clockActive)
        assertEquals(defaultPlayerTime.toSeconds(), viewState.playerTimes[PLAYER_ONE])
        assertEquals(defaultPlayerTime.toSeconds(), viewState.playerTimes[PLAYER_TWO])
        assertEquals(NO_PLAYER, viewState.activePlayer)
    }

    @Test
    fun `Given new viewModel, when player touches his clock and no player was active, then other player should become the active player`() {
        sut.process(Event.PlayerClockTouched(PLAYER_ONE))

        val viewEffect = sut.viewEffects().getOrAwaitValue()
        val viewState = sut.viewStates().getOrAwaitValue()

        assertTrue(viewState.clockActive)
        assertEquals(PLAYER_TWO, viewState.activePlayer)
        assertTrue(viewEffect is ViewEffect.PlayClockClickSound)
    }
}