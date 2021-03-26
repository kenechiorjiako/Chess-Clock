package com.skylex_chess_clock.chessclock.viewmodels


import android.util.Log
import com.skylex_chess_clock.chessclock.R
import com.skylex_chess_clock.chessclock.viewmodels.SplashScreenVM.*
import com.skylex_chess_clock.chessclock.viewmodels.SplashScreenVM.ViewNavigation.*
import com.skylex_chess_clock.news_feed.util.MviViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit

class SplashScreenVM : MviViewModel<Any, ViewEffect, ViewNavigation, Event, Any>() {



    override fun reduceToViewState(stateChange: Any) {
        TODO("Not yet implemented")
    }


    override fun process(viewEvent: Event) {
        Log.d(TAG, "process: called")
        when(viewEvent) {
            is Event.PageActive -> handlePageActiveEvent()
        }
    }
    private fun handlePageActiveEvent() {
        Log.d(TAG, "handlePageActiveEvent: called")
        firstLoadOccurred = true
        disposables.add(
            Observable.timer(3, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {

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