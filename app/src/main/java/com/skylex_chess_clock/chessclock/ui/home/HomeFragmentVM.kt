package com.skylex_chess_clock.chessclock.ui.home

import android.os.Bundle
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.skylex_chess_clock.chessclock.*
import com.skylex_chess_clock.chessclock.data.UserPreferencesRepo
import com.skylex_chess_clock.chessclock.util.PreferenceKeys
import com.skylex_chess_clock.chessclock.util.TimeHelper
import com.skylex_chess_clock.chessclock.util.TopLevelFiles.Companion.ClockMode
import com.skylex_chess_clock.chessclock.ui.home.HomeFragmentVM.*
import com.skylex_chess_clock.chessclock.ui.home.HomeFragmentVM.Event.*
import com.skylex_chess_clock.chessclock.ui.home.HomeFragmentVM.PartialStateChange.*
import com.skylex_chess_clock.news_feed.util.MviViewModel
import dagger.assisted.Assisted
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class HomeFragmentVM @Inject constructor(
    private val userPreferencesRepo: UserPreferencesRepo
) : MviViewModel<ViewState, ViewEffect, ViewNavigation, Event, PartialStateChange>() {

    private lateinit var currentClockMode: ClockMode
    private lateinit var currentTime: TimeHelper
    private lateinit var currentTimeIncrement: TimeHelper
    private var clockActivated: Long = 0

    fun init() {
        firstLoadOccurred = true
        viewState = ViewState()

        disposables.add(
            Observable.interval(TIME_INCREMENT, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .filter { viewState.clockActive }
                .subscribe {
                    changeClockTimes()
                }
        )

        userPreferencesRepo.userPreferencesFlow
            .onEach{ preferences ->
                currentTime = preferences.time
                reduceToViewState(PlayerTimeChange(PLAYER_ONE, preferences.time.toSeconds()))
                reduceToViewState(PlayerTimeChange(PLAYER_TWO, preferences.time.toSeconds()))

                currentTimeIncrement = preferences.increment

                currentClockMode = preferences.clockMode

            }.launchIn(viewModelScope)
    }

    override fun reduceToViewState(stateChange: PartialStateChange) {
        viewState = when (stateChange) {
            is PlayerTimeChange -> {
                viewState.copy(playerTimes = viewState.playerTimes.also {
                    it[stateChange.player] = stateChange.time
                })
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
            is PlayerClockTouched -> handlePlayerClockClicked(viewEvent.player)
            is RefreshClocks -> handleRefreshClocks()
            is PlayButtonClicked -> handlePlayButtonClicked()
            is PauseButtonClicked -> handlePauseButtonClicked()
            is SettingsChangeEvent -> handleSettingsChangeEvent(viewEvent.clockMode, viewEvent.time, viewEvent.increment)
            is OnSettingsButtonClicked -> handleSettingsButtonClicked()
        }
    }


    private fun handlePlayerClockClicked(player: Int) {
        if (!viewState.gameOver()) {
            if (viewState.clockActive) {
                if (player == viewState.activePlayer) {

                    when(player) {
                        PLAYER_ONE -> {
                            changeActivePlayer(
                                currentPlayer = PLAYER_ONE,
                                newPlayer = PLAYER_TWO,
                                playSound = true,
                                incrementCurrentPlayerTime = true
                            )
                        }
                        PLAYER_TWO -> {
                            changeActivePlayer(
                                currentPlayer = PLAYER_TWO,
                                newPlayer = PLAYER_ONE,
                                playSound = true,
                                incrementCurrentPlayerTime = true
                            )
                        }
                    }

                }
            } else if (viewState.activePlayer == NO_PLAYER || viewState.activePlayer == player) {
                reduceToViewState(ClockActivityChange(true))
                when(player) {
                    PLAYER_ONE -> {
                        changeActivePlayer(
                            currentPlayer = PLAYER_ONE,
                            newPlayer = PLAYER_TWO,
                            playSound = true,
                            incrementCurrentPlayerTime = false
                        )
                    }
                    PLAYER_TWO -> {
                        changeActivePlayer(
                            currentPlayer = PLAYER_TWO,
                            newPlayer = PLAYER_ONE,
                            playSound = true,
                            incrementCurrentPlayerTime = false
                        )
                    }
                }
            }
        }
    }
    private fun handleRefreshClocks() {
        reduceToViewState(PlayerTimeChange(PLAYER_ONE, currentTime.toSeconds()))
        reduceToViewState(PlayerTimeChange(PLAYER_TWO, currentTime.toSeconds()))
        reduceToViewState(ActivePlayerChange(NO_PLAYER))
        reduceToViewState(ClockActivityChange(false))
    }
    private fun handlePlayButtonClicked() {
        reduceToViewState(ClockActivityChange(true))
    }
    private fun handlePauseButtonClicked() {
        reduceToViewState(ClockActivityChange(false))
    }
    private fun handleSettingsChangeEvent(clockMode: ClockMode, time: TimeHelper, increment: TimeHelper) {
        viewModelScope.launch {
            userPreferencesRepo.updateUserSettings(clockMode, time, increment)
        }
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
        if (viewState.clockActive && viewState.activePlayer != NO_PLAYER) {
            when(currentClockMode) {
                ClockMode.SUDDEN_DEATH,
                ClockMode.INCREMENT -> {
                    decreasePlayerTime(viewState.activePlayer)
                }
                ClockMode.SIMPLE_DELAY -> {
                    if (System.currentTimeMillis() - clockActivated < SIMPLE_DELAY_MILLIS) {
                        return
                    } else {
                        decreasePlayerTime(viewState.activePlayer)
                    }
                }
                ClockMode.HOURGLASS -> {
                    if (viewState.playerTimes.getValue(viewState.activePlayer) > NO_TIME) {
                        if (viewState.activePlayer == PLAYER_ONE) {
                            decreasePlayerTime(PLAYER_ONE)
                            increasePlayerTime(PLAYER_TWO)
                        } else if (viewState.activePlayer == PLAYER_TWO) {
                            decreasePlayerTime(PLAYER_TWO)
                            increasePlayerTime(PLAYER_ONE)
                        }
                    }
                }
            }
        }
    }
    private fun decreasePlayerTime(player: Int) {
        val currentPlayerTime = viewState.playerTimes.getValue(player)
        if (currentPlayerTime > NO_TIME) {
            reduceToViewState(PlayerTimeChange(player, currentPlayerTime - TIME_INCREMENT))
            if (viewState.playerTimes.getValue(player) == NO_TIME) {
                viewEffect = ViewEffect.PlayClockTimeoutSound
            }
        }
    }
    private fun increasePlayerTime(player: Int) {
        reduceToViewState(PlayerTimeChange(player, viewState.playerTimes.getValue(player) + TIME_INCREMENT))
    }
    private fun incrementPlayerTime(player: Int) {
        val timeIncrementInSeconds = currentTimeIncrement.toSeconds()
        val currentPlayerTime = viewState.playerTimes.getValue(player)

        if (currentClockMode == ClockMode.INCREMENT || currentClockMode == ClockMode.SIMPLE_DELAY) {
            reduceToViewState(PlayerTimeChange(player, currentPlayerTime + timeIncrementInSeconds))
        }
    }
    private fun changeActivePlayer(currentPlayer: Int, newPlayer: Int, playSound: Boolean, incrementCurrentPlayerTime: Boolean) {
        if (playSound) viewEffect =  ViewEffect.PlayClockClickSound
        if (incrementCurrentPlayerTime) {
            incrementPlayerTime(currentPlayer)
        }
        clockActivated = System.currentTimeMillis()
        reduceToViewState(ActivePlayerChange(newPlayer))
    }




    data class ViewState(
        val playerTimes: HashMap<Int, Long> = hashMapOf(Pair(PLAYER_ONE, 300), Pair(PLAYER_TWO, 300)),
        val activePlayer: Int = NO_PLAYER,
        val clockActive: Boolean = false
    ) {
        fun gameOver(): Boolean = playerTimes.getValue(PLAYER_ONE) == NO_TIME || playerTimes.getValue(PLAYER_TWO) == NO_TIME
    }
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
        data class PlayerClockTouched(val player: Int) : Event()
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
        const val TIME_INCREMENT = 1L
        const val NO_TIME = 0L
        val SIMPLE_DELAY_MILLIS = TimeUnit.SECONDS.toMillis(2)
    }
}