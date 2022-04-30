package com.skylex_chess_clock.chessclock.ui.home

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.skylex_chess_clock.chessclock.*
import com.skylex_chess_clock.chessclock.databinding.FragmentHomeBinding
import com.skylex_chess_clock.chessclock.ui.settings.SettingsBottomSheetFragment
import com.skylex_chess_clock.chessclock.util.TimeHelper
import com.skylex_chess_clock.chessclock.util.TopLevelFiles.Companion.ClockMode
import com.skylex_chess_clock.chessclock.ui.home.HomeFragmentVM.*
import com.skylex_chess_clock.chessclock.ui.home.HomeFragmentVM.Companion.NO_PLAYER
import com.skylex_chess_clock.chessclock.ui.home.HomeFragmentVM.Companion.PLAYER_ONE
import com.skylex_chess_clock.chessclock.ui.home.HomeFragmentVM.Companion.PLAYER_TWO
import com.skylex_chess_clock.news_feed.util.MviFragment
import dagger.hilt.android.AndroidEntryPoint
import org.parceler.Parcels
import java.util.*


@AndroidEntryPoint
class HomeFragment : MviFragment<ViewState, ViewEffect, ViewNavigation, Event, PartialStateChange, HomeFragmentVM>() {


    private lateinit var binding: FragmentHomeBinding
    private var mediaPlayer: MediaPlayer? = null
    override val viewModel: HomeFragmentVM by viewModels()

    override fun onPause() {
        super.onPause()
        viewModel.viewStates().value?.let { viewState ->
            if (!viewState.gameOver() && viewState.clockActive) {
                mEvents.onNext(Event.PauseButtonClicked)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)

        observeFragmentResults()

        if (savedInstanceState == null) {
            viewModel.init()
            startEnterTransition()
        }

        return binding.root
    }

    private fun startEnterTransition() {

    }

    override fun setupViewHelperObjects() {

    }
    override fun setupViews() {
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun setupViewListeners() {
        binding.apply {
            playButton.setOnClickListener {
                mEvents.onNext(Event.PlayButtonClicked)
            }
            pauseButton.setOnClickListener {
                mEvents.onNext(Event.PauseButtonClicked)
            }
            refreshButton.setOnClickListener {
                mEvents.onNext(Event.RefreshClocks)
            }
            settingsButton.setOnClickListener {
                mEvents.onNext(Event.OnSettingsButtonClicked)
            }

            playerOneSection.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    mEvents.onNext(Event.PlayerClockTouched(PLAYER_ONE))
                }
                true
            }
            playerTwoSection.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    mEvents.onNext(Event.PlayerClockTouched(PLAYER_TWO))
                }
                true
            }
        }
    }

    private fun observeFragmentResults() {
        childFragmentManager.setFragmentResultListener(
            SettingsBottomSheetFragment.RESULT_KEY,
            viewLifecycleOwner
        ) { _, result ->
            val clockMode: ClockMode = result.getSerializable(CLOCK_MODE_RESULT_KEY) as ClockMode
            val time: TimeHelper = Parcels.unwrap(result.getParcelable(TIME_RESULT_KEY))
            val increment: TimeHelper = Parcels.unwrap(result.getParcelable(INCREMENT_RESULT_KEY))
            mEvents.onNext(Event.SettingsChangeEvent(time, increment, clockMode))
        }
    }

    override fun renderViewState(viewState: ViewState) {
        binding.apply {

            val gameInactive = !viewState.clockActive || viewState.gameOver()
            refreshButton.isClickable = gameInactive
            settingsButton.isClickable = gameInactive

            if (viewState.clockActive) {
                clockActivityDimmer.visibility = GONE
                pauseButton.visibility = VISIBLE
                playButton.visibility = GONE

                if (viewState.gameOver()) {
                    refreshButton.alpha = 1f
                    settingsButton.alpha = 1f
                    pauseButton.alpha = 0.5f
                    pauseButton.isClickable = false
                } else {
                    refreshButton.alpha = 0.5f
                    settingsButton.alpha = 0.5f
                    pauseButton.alpha = 1f
                    pauseButton.isClickable = true
                }

            } else {
                clockActivityDimmer.visibility = VISIBLE
                pauseButton.visibility = GONE
                playButton.visibility = VISIBLE
                playButton.isClickable = viewState.activePlayer != NO_PLAYER

                if (viewState.activePlayer == NO_PLAYER) {
                    playButton.alpha = 0.5f
                } else {
                    playButton.alpha = 1f
                }

                refreshButton.alpha = 1f
                settingsButton.alpha = 1f
            }

            when (viewState.activePlayer) {
                NO_PLAYER -> {
                    playerTwoTime.setTextColor(resources.getColor(R.color.time_color_inactive))
                    playerOneTime.setTextColor(resources.getColor(R.color.time_color_inactive))
                    playerOneSection.setBackgroundResource(R.color.color_transparent)
                    playerTwoSection.setBackgroundResource(R.color.color_transparent)
                }
                PLAYER_ONE -> {
                    playerTwoTime.setTextColor(resources.getColor(R.color.time_color_inactive))
                    playerOneTime.setTextColor(resources.getColor(R.color.white))
                    if (viewState.playerTimes.getValue(PLAYER_ONE) == 0L) {
                        playerOneSection.setBackgroundResource(R.color.color_timeout_red)
                    } else {
                        playerOneSection.setBackgroundResource(R.color.color_primary)
                    }
                    playerTwoSection.setBackgroundResource(R.color.color_transparent)
                }
                PLAYER_TWO -> {
                    playerTwoTime.setTextColor(resources.getColor(R.color.white))
                    playerOneTime.setTextColor(resources.getColor(R.color.time_color_inactive))
                    playerOneSection.setBackgroundResource(R.color.color_transparent)
                    if (viewState.playerTimes.getValue(PLAYER_TWO) == 0L) {
                        playerTwoSection.setBackgroundResource(R.color.color_timeout_red)
                    } else {
                        playerTwoSection.setBackgroundResource(R.color.color_primary)
                    }
                }
            }

            if (viewState.playerTimes.getValue(PLAYER_ONE) == 0L) {
                playerOneTime.visibility = GONE
                playerOneTimeoutTextView.visibility = VISIBLE
            } else {
                playerOneTime.visibility = VISIBLE
                playerOneTimeoutTextView.visibility = GONE
            }
            if (viewState.playerTimes.getValue(PLAYER_TWO) == 0L) {
                playerTwoTime.visibility = GONE
                playerTwoTimeoutTextView.visibility = VISIBLE
            } else {
                playerTwoTime.visibility = VISIBLE
                playerTwoTimeoutTextView.visibility = GONE
            }
            playerTwoTime.text = getTimeTextFromSeconds(viewState.playerTimes.getValue(PLAYER_TWO))
            playerOneTime.text = getTimeTextFromSeconds(viewState.playerTimes.getValue(PLAYER_ONE))
        }
    }

    private fun getTimeTextFromSeconds(seconds: Long) : String {
        var timeText = ""

        val hours = seconds / 3600
        val minutes = seconds % 3600 / 60
        val secs = seconds % 60

        timeText = if (hours > 0) {
            String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs)
        } else {
            String.format(Locale.getDefault(), "%d:%02d", minutes, secs)
        }

        return timeText
    }

    override fun renderViewEffect(viewEffect: ViewEffect) {
        when (viewEffect) {
            is ViewEffect.PlayClockClickSound -> {
                Log.d(TAG, "renderViewEffect: called")
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer.create(context, R.raw.button_click_sound)
                mediaPlayer?.start()
            }
            is ViewEffect.PlayClockTimeoutSound -> {
                Log.d(TAG, "renderViewEffect: called")
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer.create(context, R.raw.timeout_sound)
                mediaPlayer?.start()
            }
        }
    }
    override fun handleViewNavigation(viewNavigation: ViewNavigation) {
        when (viewNavigation) {
            is ViewNavigation.NavigateToSettingsFragment -> navigateToSettingsBottomSheet(viewNavigation.outBundle)
        }
    }

    private fun navigateToSettingsBottomSheet(outBundle: Bundle) {
        val bottomSheetFragment = SettingsBottomSheetFragment()
        bottomSheetFragment.arguments = outBundle
        bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
    }

    override fun dispatchLoadPageEvent() {
    }

    companion object {
        private const val TAG = "HomeFragment"
    }
}