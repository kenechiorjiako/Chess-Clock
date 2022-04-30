package com.skylex_chess_clock.chessclock.ui.splash



import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.skylex_chess_clock.chessclock.R
import com.skylex_chess_clock.chessclock.ui.splash.SplashScreenVM.*
import com.skylex_chess_clock.chessclock.ui.splash.SplashScreenVM.ViewEffect.*
import com.skylex_chess_clock.chessclock.ui.splash.SplashScreenVM.ViewNavigation.*
import com.skylex_chess_clock.news_feed.util.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class SplashScreenVM @Inject constructor(): MviViewModel<Any, ViewEffect, ViewNavigation, Event, Any>() {


    override fun reduceToViewState(stateChange: Any) {
        // No implementation
    }


    override fun process(viewEvent: Event) {
        when(viewEvent) {
            is Event.PageActive -> handlePageActiveEvent()
        }
    }


    private fun handlePageActiveEvent() {
        firstLoadOccurred = true
        disposables.add (
            Observable.timer(3, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    viewEffect = ShowLogo
                }
                .subscribe {
                    viewNavigation = NavigateToFragment(R.id.homeFragment)
                }
        )
    }


    sealed class Event {
        object PageActive: Event()
    }
    sealed class ViewEffect {
        object ShowLogo: ViewEffect()
    }
    sealed class ViewNavigation {
        data class NavigateToFragment(val pageId: Int): ViewNavigation()
    }

    companion object {
        private const val TAG = "SplashScreenVM"
    }
}