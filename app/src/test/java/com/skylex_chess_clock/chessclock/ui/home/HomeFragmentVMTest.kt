package com.skylex_chess_clock.chessclock.ui.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.SavedStateHandle
import com.skylex_chess_clock.chessclock.ui.home.HomeFragmentVM.Companion.NO_PLAYER
import com.skylex_chess_clock.chessclock.ui.home.HomeFragmentVM.Companion.NO_TIME
import com.skylex_chess_clock.chessclock.ui.home.HomeFragmentVM.Companion.PLAYER_ONE
import com.skylex_chess_clock.chessclock.ui.home.HomeFragmentVM.Companion.PLAYER_TWO
import com.skylex_chess_clock.chessclock.ui.home.HomeFragmentVM.Companion.TIME_INCREMENT
import com.skylex_chess_clock.chessclock.ui.home.HomeFragmentVM.Event
import com.skylex_chess_clock.chessclock.ui.home.HomeFragmentVM.ViewEffect
import com.skylex_chess_clock.chessclock.util.*
import com.skylex_chess_clock.chessclock.util.TopLevelFiles.Companion.ClockMode
import io.mockk.*
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.TestScheduler
import junit.framework.TestCase.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class HomeFragmentVMTest {

    @get:Rule
    val liveDataRule = InstantTaskExecutorRule()

    @get:Rule
    val schedulers = RxImmediateSchedulerRule()



    private val mockDataStore = mockk<DataStore<Preferences>>()
    private val mockSavedStateHandle = mockk<SavedStateHandle>()
    private val mockPreferences = mockk<MutablePreferences>()

    private val defaultPlayerTime = TimeHelper(5, TimeUnit.MINUTES)
    private val defaultTimeIncrement = TimeHelper(2, TimeUnit.SECONDS)
    private val defaultClockMode = ClockMode.SUDDEN_DEATH.name

    private val sut = HomeFragmentVM(
        mockSavedStateHandle,
        mockDataStore
    )

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { mockDataStore.data } returns flowOf(mockPreferences)
        every { mockPreferences[PreferenceKeys.timeHelperPreferenceKey] } returns TimeHelper.TimeHelperPreferencesConverter.serialize(defaultPlayerTime)
        every { mockPreferences[PreferenceKeys.timeIncrementPreferenceKey] } returns TimeHelper.TimeHelperPreferencesConverter.serialize(defaultTimeIncrement)
        every { mockPreferences[PreferenceKeys.clockModePreferenceKey] } returns defaultClockMode
    }

    @Test
    fun `Given new viewModel, then data should be set appropriately`() {
        runTest {
            sut.init()

            val viewState = sut.viewStates().getOrAwaitValue()

            assertTrue(sut.firstLoadOccurred)
            assertFalse(viewState.clockActive)
            assertEquals(defaultPlayerTime.toSeconds(), viewState.playerTimes[PLAYER_ONE])
            assertEquals(defaultPlayerTime.toSeconds(), viewState.playerTimes[PLAYER_TWO])
            assertEquals(NO_PLAYER, viewState.activePlayer)
        }
    }

    @Test
    fun `Given new viewModel, when player touches his clock and no player was active, then other player should become the active player`() {
        sut.init()

        sut.process(Event.PlayerClockTouched(PLAYER_ONE))

        val viewEffect = sut.viewEffects().getOrAwaitValue()
        val viewState = sut.viewStates().getOrAwaitValue()

        assertTrue(viewState.clockActive)
        assertEquals(PLAYER_TWO, viewState.activePlayer)
        assertTrue(viewEffect is ViewEffect.PlayClockClickSound)
    }

    @Test
    fun `Given new viewModel, when player touches his clock and no player was active, then second player time should countdown`() {
        val testScheduler = TestScheduler()
        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }

        sut.init()
        sut.process(Event.PlayerClockTouched(PLAYER_ONE))

        testScheduler.advanceTimeBy(TIME_INCREMENT * 5, TimeUnit.SECONDS)

        val viewEffect = sut.viewEffects().getOrAwaitValue()
        val viewState = sut.viewStates().getOrAwaitValue()

        assertTrue(viewState.clockActive)
        assertEquals(defaultPlayerTime.toSeconds() - (TIME_INCREMENT * 5), viewState.playerTimes[PLAYER_TWO])
        assertEquals(PLAYER_TWO, viewState.activePlayer)
        assertTrue(viewEffect is ViewEffect.PlayClockClickSound)
    }

    @Test
    fun `Given new viewModel and clock mode is hourglass, when player touches his clock and no player was active, then second player time should countdown`() {
        every { mockPreferences[PreferenceKeys.clockModePreferenceKey] } returns ClockMode.HOURGLASS.name

        val testScheduler = TestScheduler()
        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }

        sut.init()
        sut.process(Event.PlayerClockTouched(PLAYER_ONE))

        testScheduler.advanceTimeBy(TIME_INCREMENT * 5, TimeUnit.SECONDS)

        val viewEffect = sut.viewEffects().getOrAwaitValue()
        val viewState = sut.viewStates().getOrAwaitValue()

        assertTrue(viewState.clockActive)
        assertEquals(defaultPlayerTime.toSeconds() - (TIME_INCREMENT * 5), viewState.playerTimes[PLAYER_TWO])
        assertEquals(defaultPlayerTime.toSeconds() + (TIME_INCREMENT * 5), viewState.playerTimes[PLAYER_ONE])
        assertEquals(PLAYER_TWO, viewState.activePlayer)
        assertTrue(viewEffect is ViewEffect.PlayClockClickSound)
    }

    // Test SIMPLE delay clock changes



    @Test
    fun `Given viewModel, when play button clicked, then change clockActivity state to true`() {
        sut.init()
        var viewState = sut.viewStates().getOrAwaitValue()

        assertFalse(viewState.clockActive)

        sut.process(Event.PlayButtonClicked)
        viewState = sut.viewStates().getOrAwaitValue()

        assertTrue(viewState.clockActive)
    }

    @Test
    fun `Given viewModel, when pause button clicked, then change clockActivity state to false`() {
        sut.init()
        var viewState = sut.viewStates().getOrAwaitValue()

        assertFalse(viewState.clockActive)

        sut.process(Event.PlayButtonClicked)
        viewState = sut.viewStates().getOrAwaitValue()

        assertTrue(viewState.clockActive)

        sut.process(Event.PauseButtonClicked)
        viewState = sut.viewStates().getOrAwaitValue()

        assertFalse(viewState.clockActive)
    }

    @Test
    fun `Given viewModel, when settings changed, then preferences should be set appropriately`() {
        sut.init()

        val clockMode = ClockMode.SUDDEN_DEATH
        val time = TimeHelper(15, TimeUnit.MINUTES)
        val increment = TimeHelper(1, TimeUnit.SECONDS)

        sut.process(Event.PlayButtonClicked)
        sut.process(Event.SettingsChangeEvent(clockMode = clockMode, time = time, increment = increment))

        val viewState = sut.viewStates().getOrAwaitValue()

        assertEquals(defaultPlayerTime.toSeconds(), viewState.playerTimes[PLAYER_ONE])
        assertEquals(defaultPlayerTime.toSeconds(), viewState.playerTimes[PLAYER_TWO])
        assertEquals(NO_PLAYER, viewState.activePlayer)
        assertFalse(viewState.clockActive)
    }

    @Test
    fun `Given new viewModel, when player touches his clock and second player time elapses, then play clock timeout sound`() {
        val testScheduler = TestScheduler()
        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }

        sut.init()
        sut.process(Event.PlayerClockTouched(PLAYER_ONE))
        var viewEffect = sut.viewEffects().getOrAwaitValue()
        assertTrue(viewEffect is ViewEffect.PlayClockClickSound)

        testScheduler.advanceTimeBy(TIME_INCREMENT * defaultPlayerTime.toSeconds(), TimeUnit.SECONDS)
        viewEffect = sut.viewEffects().getOrAwaitValue()
        val viewState = sut.viewStates().getOrAwaitValue()

        assertTrue(viewState.clockActive)
        assertEquals(NO_TIME, viewState.playerTimes[PLAYER_TWO])
        assertEquals(PLAYER_TWO, viewState.activePlayer)
        assertTrue(viewEffect is ViewEffect.PlayClockTimeoutSound)
    }

    @Test
    fun `Given viewModel, when refreshClockClick, then view state should be reset`() {
        val testScheduler = TestScheduler()
        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }

        val playerTime = TimeHelper(6, TimeUnit.MINUTES)
        every { mockPreferences[PreferenceKeys.timeHelperPreferenceKey] } returns TimeHelper.TimeHelperPreferencesConverter.serialize(playerTime)

        sut.init()
        sut.process(Event.PlayerClockTouched(PLAYER_ONE))
        testScheduler.advanceTimeBy(10, TimeUnit.SECONDS)
        sut.process(Event.RefreshClocks)


        val viewState = sut.viewStates().getOrAwaitValue()

        assertEquals(playerTime.toSeconds(), viewState.playerTimes[PLAYER_ONE])
        assertEquals(playerTime.toSeconds(), viewState.playerTimes[PLAYER_TWO])
        assertEquals(NO_PLAYER, viewState.activePlayer)
        assertFalse(viewState.clockActive)

    }
}