package com.skylex_chess_clock.news_feed.util

interface ViewModelContract<EVENT, STATE_CHANGE> {

    fun process(viewEvent: EVENT)
    fun reduceToViewState(stateChange: STATE_CHANGE)
}