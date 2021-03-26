package com.skylex_chess_clock.chessclock.viewmodels

import android.os.Bundle
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.skylex_chess_clock.chessclock.*
import com.skylex_chess_clock.chessclock.util.PreferenceKeys
import com.skylex_chess_clock.chessclock.util.TimeHelper
import com.skylex_chess_clock.chessclock.util.TopLevelFiles.Companion.ClockMode
import com.skylex_chess_clock.chessclock.viewmodels.HomeFragmentVM.*
import com.skylex_chess_clock.chessclock.viewmodels.HomeFragmentVM.Event.*
import com.skylex_chess_clock.chessclock.viewmodels.HomeFragmentVM.PartialStateChange.*
import com.skylex_chess_clock.news_feed.util.MviViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class HomeFragmentVM @ViewModelInject constructor(
        @Assisted savedStateHandle: SavedStateHandle,
) : MviViewModel<ViewState, ViewEffect, ViewNavigation, Event, PartialStateChange>(){

    private val clockTimeObservable : Observable<Long>?
    private val playerTwoTimeObservable : Observable<Long>?

    private var currentTime: TimeHelper? = null
    private var currentClockMode: ClockMode? = null
    private var currentTimeIncrement: TimeHelper? = null
    private var clockActivated: Long = 0

    init {
        viewState = ViewState(playerOneTime = 20, playerTwoTime = 20)

        clockTimeObservable = Observable.interval(1, TimeUnit.SECONDS, Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

        playerTwoTimeObservable = Observable.interval(1, TimeUnit.SECONDS, Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

        disposables.addAll(
            clockTimeObservable.subscribe {
                changeClockTimes()
            }

//            playerTwoTimeObservable.subscribe {
//                Log.d(TAG, "observing 2")
//                if (viewState.clockActive && viewState.activePlayer == PLAYER_TWO && viewState.playerTwoTime != 0L) {
//                    Log.d(Companion.TAG, "observing player two time change")
//                    reduceToViewState(PlayerTimeChange(PLAYER_TWO, viewState.playerTwoTime - 1))
//                }
//            },
        )

        MyApplication.dataStore.data.map {
            preferences ->
            val timeString = preferences[PreferenceKeys.timeHelperPreferenceKey] ?: TimeHelper.TimeHelperPreferencesConverter.serialize(
                    TimeHelper(5, TimeUnit.MINUTES)
            )
            return@map TimeHelper.TimeHelperPreferencesConverter.deserialize(timeString)
        }.take(count = 1).onEach {
            currentTime = it
            reduceToViewState(PlayerTimeChange(PLAYER_ONE, currentTime?.timeUnit?.toSeconds(currentTime?.time!!)!!))
            reduceToViewState(PlayerTimeChange(PLAYER_TWO, currentTime?.timeUnit?.toSeconds(currentTime?.time!!)!!))
        }.launchIn(viewModelScope)

        MyApplication.dataStore.data.map {
            preferences ->
            val timeString = preferences[PreferenceKeys.timeIncrementPreferenceKey] ?: TimeHelper.TimeHelperPreferencesConverter.serialize(
                    TimeHelper(2, TimeUnit.SECONDS)
            )
            return@map TimeHelper.TimeHelperPreferencesConverter.deserialize(timeString)
        }.take(count = 1).onEach {
            currentTimeIncrement = it
        }.launchIn(viewModelScope)

        MyApplication.dataStore.data.map {
            preferences ->
            val clockModeString = preferences[PreferenceKeys.clockModePreferenceKey] ?: ClockMode.Sudden_Death.name
            return@map ClockMode.getClockModeFromName(clockModeString)
        }.onEach {
            currentClockMode = it
        }.launchIn(viewModelScope)
    }

    override fun reduceToViewState(stateChange: PartialStateChange) {
        viewState = when (stateChange) {
            is PlayerTimeChange -> {
                if (stateChange.player == PLAYER_ONE) {
                    viewState.copy(playerOneTime = stateChange.time)
                } else {
                    viewState.copy(playerTwoTime = stateChange.time)
                }
            }
            is ActivePlayerChange -> {
                viewState.copy(activePlayer = stateChange.activePlayer)
            }
            is ClockActivityChange -> {
                viewState.copy(clockActive = stateChange.clockActive)
            }
        }
    }

    override fun process(viewEvent: Event) {
        when (viewEvent) {
            is PlayerClockClicked -> handlePlayerClockClicked(viewEvent.player)
            is RefreshClocks -> handleRefreshClocks()
            is PlayButtonClicked -> handlePlayButtonClicked()
            is PauseButtonClicked -> handlePauseButtonClicked()
            is SettingsChangeEvent -> handleSettingsChangeEvent(viewEvent.clockMode, viewEvent.time, viewEvent.increment)
            is OnSettingsButtonClicked -> handleSettingsButtonClicked()
        }
    }


    private fun handlePlayerClockClicked(player: Int) {
        if (viewState.playerOneTime != 0L && viewState.playerTwoTime != 0L) {
            if (viewState.clockActive) {
                if (player == viewState.activePlayer) {
                    viewEffect = ViewEffect.PlayClockClickSound
                    stopClock(player)
                    if (player != PLAYER_TWO) {
                        startClock(PLAYER_TWO)
                        reduceToViewState(ActivePlayerChange(PLAYER_TWO))
                    } else {
                        startClock(PLAYER_ONE)
                        reduceToViewState(ActivePlayerChange(PLAYER_ONE))
                    }
                }
            } else {
                if (viewState.activePlayer == NO_PLAYER) {
                    viewEffect = ViewEffect.PlayClockClickSound
                    reduceToViewState(ClockActivityChange(true))
                    if (player == PLAYER_ONE) {
                        startClock(PLAYER_TWO)
                        reduceToViewState(ActivePlayerChange(PLAYER_TWO))
                    } else {
                        startClock(PLAYER_ONE)
                        reduceToViewState(ActivePlayerChange(PLAYER_ONE))
                    }
                } else if (player == viewState.activePlayer) {
                    viewEffect = ViewEffect.PlayClockClickSound
                    reduceToViewState(ClockActivityChange(true))
                    if (player == PLAYER_ONE) {
                        startClock(PLAYER_TWO)
                        reduceToViewState(ActivePlayerChange(PLAYER_TWO))
                    } else {
                        startClock(PLAYER_ONE)
                        reduceToViewState(ActivePlayerChange(PLAYER_ONE))
                    }
                }
            }
        }
    }
    private fun handleRefreshClocks() {
        if (currentTime != null) {
            reduceToViewState(PlayerTimeChange(PLAYER_ONE, currentTime?.timeUnit?.toSeconds(currentTime?.time!!)!!))
            reduceToViewState(PlayerTimeChange(PLAYER_TWO, currentTime?.timeUnit?.toSeconds(currentTime?.time!!)!!))
            reduceToViewState(ActivePlayerChange(NO_PLAYER))
            reduceToViewState(ClockActivityChange(false))
        }
    }
    private fun handlePlayButtonClicked() {
        reduceToViewState(ClockActivityChange(true))
    }
    private fun handlePauseButtonClicked() {
        reduceToViewState(ClockActivityChange(false))
    }
    private fun handleSettingsChangeEvent(clockMode: ClockMode, time: TimeHelper, increment: TimeHelper) {
        Log.d(TAG, "handleSettingsChangeEvent: got result for clock mode -> $clockMode")
        Log.d(TAG, "handleSettingsChangeEvent: got result for time of $time")
        viewModelScope.launch {
            MyApplication.dataStore.edit {
                    preferences ->
                preferences[PreferenceKeys.timeHelperPreferenceKey] = TimeHelper.TimeHelperPreferencesConverter.serialize(time)
                preferences[PreferenceKeys.timeIncrementPreferenceKey] = TimeHelper.TimeHelperPreferencesConverter.serialize(increment)
                preferences[PreferenceKeys.clockModePreferenceKey] = clockMode.name
            }
        }
        currentTime = time
        currentClockMode = clockMode
        currentTimeIncrement = increment
        handleRefreshClocks()
    }
    private fun handleSettingsButtonClicked() {
        val outBundle = Bundle()
        outBundle.putSerializable(CLOCK_MODE_RESULT_KEY, currentClockMode)
        outBundle.putParcelable(TIME_RESULT_KEY, currentTime)
        outBundle.putParcelable(INCREMENT_RESULT_KEY, currentTimeIncrement)
        viewNavigation = ViewNavigation.NavigateToSettingsFragment(outBundle)
    }



    private fun changeClockTimes() {
        if (viewState.clockActive) {

//            when(currentClockMode) {
//                ClockMode.Sudden_Death -> {
//                    if (viewState.activePlayer == PLAYER_ONE && viewState.playerOneTime != 0L) {
//                        reduceToViewState(PlayerTimeChange(PLAYER_ONE, viewState.playerOneTime - 1))
//                    } else if (viewState.activePlayer == PLAYER_TWO && viewState.playerTwoTime != 0L) {
//                        reduceToViewState(PlayerTimeChange(PLAYER_TWO, viewState.playerTwoTime - 1))
//                    }
//                }
//                ClockMode.Increment -> {
//                    if (viewState.activePlayer == PLAYER_ONE && viewState.playerOneTime != 0L) {
//                        reduceToViewState(PlayerTimeChange(PLAYER_ONE, viewState.playerOneTime - 1))
//                    } else if (viewState.activePlayer == PLAYER_TWO && viewState.playerTwoTime != 0L) {
//                        reduceToViewState(PlayerTimeChange(PLAYER_TWO, viewState.playerTwoTime - 1))
//                    }
//                }
//                ClockMode.Hourglass -> {
//                    if (viewState.activePlayer == PLAYER_ONE && viewState.playerOneTime != 0L) {
//                        reduceToViewState(PlayerTimeChange(PLAYER_ONE, viewState.playerOneTime - 1))
//                        reduceToViewState(PlayerTimeChange(PLAYER_TWO, viewState.playerTwoTime + 1))
//                    } else if (viewState.activePlayer == PLAYER_TWO && viewState.playerTwoTime != 0L) {
//                        reduceToViewState(PlayerTimeChange(PLAYER_TWO, viewState.playerTwoTime - 1))
//                        reduceToViewState(PlayerTimeChange(PLAYER_ONE, viewState.playerOneTime + 1))
//                    }
//                }
//                ClockMode.Simple_Delay -> {
//                    if (System.currentTimeMillis() - clockActivated >= SIMPLE_DELAY_MILLIS) {
//                        if (viewState.activePlayer == PLAYER_ONE && viewState.playerOneTime != 0L) {
//                            reduceToViewState(PlayerTimeChange(PLAYER_ONE, viewState.playerOneTime - 1))
//                        } else if (viewState.activePlayer == PLAYER_TWO && viewState.playerTwoTime != 0L) {
//                            reduceToViewState(PlayerTimeChange(PLAYER_TWO, viewState.playerTwoTime - 1))
//                        }
//                    }
//                }
//            }

            if (currentClockMode == ClockMode.Sudden_Death || currentClockMode == ClockMode.Increment || currentClockMode == ClockMode.Simple_Delay) {
                if (currentClockMode == ClockMode.Simple_Delay && (System.currentTimeMillis() - clockActivated < SIMPLE_DELAY_MILLIS)) {
                    return
                } else {
                    if (viewState.activePlayer == PLAYER_ONE && viewState.playerOneTime != 0L) {
                        reduceToViewState(PlayerTimeChange(PLAYER_ONE, viewState.playerOneTime - 1))
                        if (viewState.playerOneTime == 0L) {
                            viewEffect = ViewEffect.PlayClockTimeoutSound
                        }
                    } else if (viewState.activePlayer == PLAYER_TWO && viewState.playerTwoTime != 0L) {
                        reduceToViewState(PlayerTimeChange(PLAYER_TWO, viewState.playerTwoTime - 1))
                        if (viewState.playerTwoTime == 0L) {
                            viewEffect = ViewEffect.PlayClockTimeoutSound
                        }
                    }
                }
            } else if (currentClockMode == ClockMode.Hourglass) {
                if (viewState.activePlayer == PLAYER_ONE && viewState.playerOneTime != 0L) {
                    reduceToViewState(PlayerTimeChange(PLAYER_ONE, viewState.playerOneTime - 1))
                    reduceToViewState(PlayerTimeChange(PLAYER_TWO, viewState.playerTwoTime + 1))
                    if (viewState.playerOneTime == 0L) {
                        viewEffect = ViewEffect.PlayClockTimeoutSound
                    }
                } else if (viewState.activePlayer == PLAYER_TWO && viewState.playerTwoTime != 0L) {
                    reduceToViewState(PlayerTimeChange(PLAYER_TWO, viewState.playerTwoTime - 1))
                    reduceToViewState(PlayerTimeChange(PLAYER_ONE, viewState.playerOneTime + 1))
                    if (viewState.playerTwoTime == 0L) {
                        viewEffect = ViewEffect.PlayClockTimeoutSound
                    }
                }
            }
        }
    }
    private fun startClock(player: Int) {
        clockActivated = System.currentTimeMillis()
    }
    private fun stopClock(player: Int) {
        val timeIncrementInSeconds : Long = currentTimeIncrement?.timeUnit?.toSeconds(currentTimeIncrement?.time!!)?: 0
        if (currentClockMode == ClockMode.Increment || currentClockMode == ClockMode.Simple_Delay) {
            if (player == PLAYER_ONE) {
                reduceToViewState(PlayerTimeChange(player, viewState.playerOneTime + timeIncrementInSeconds))
            } else if (player == PLAYER_TWO) {
                reduceToViewState(PlayerTimeChange(player, viewState.playerTwoTime + timeIncrementInSeconds))
            }
        }
    }


    data class ViewState(
            val playerOneTime: Long = 300,
            val playerTwoTime: Long = 300,
            val activePlayer: Int = NO_PLAYER,
            val clockActive: Boolean = false
    ) {}
    sealed class ViewEffect {
        object PlayClockClickSound: ViewEffect()
        object PlayClockTimeoutSound: ViewEffect()
    }
    sealed class ViewNavigation {
        data class NavigateToSettingsFragment(val outBundle: Bundle) : ViewNavigation()
    }
    sealed class PartialStateChange {
        data class PlayerTimeChange(val player: Int, val time: Long) : PartialStateChange()
        data class ActivePlayerChange(val activePlayer: Int) : PartialStateChange()
        data class ClockActivityChange(val clockActive: Boolean) : PartialStateChange()
    }
    sealed class Event {
        data class PlayerClockClicked(val player: Int) : Event()
        object RefreshClocks : Event()
        object PlayButtonClicked : Event()
        object PauseButtonClicked : Event()
        object OnSettingsButtonClicked : Event()
        data class SettingsChangeEvent(val time: TimeHelper, val increment: TimeHelper, val clockMode: ClockMode): Event()
    }


    companion object {
        const val NO_PLAYER = 0
        const val PLAYER_ONE = 1
        const val PLAYER_TWO = 2
        val SIMPLE_DELAY_MILLIS = TimeUnit.SECONDS.toMillis(2)
        private const val TAG = "HomeFragmentVM"
    }
}