package com.skylex_chess_clock.chessclock.ui.splash

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.skylex_chess_clock.chessclock.ui.splash.SplashScreenVM.*
import com.skylex_chess_clock.chessclock.util.getOrAwaitValue
import junit.framework.TestCase.assertEquals
import com.skylex_chess_clock.chessclock.R
import com.skylex_chess_clock.chessclock.ui.splash.SplashScreenVM.ViewEffect.*
import com.skylex_chess_clock.chessclock.ui.splash.SplashScreenVM.ViewNavigation.*
import com.skylex_chess_clock.chessclock.util.RxImmediateSchedulerRule
import junit.framework.TestCase.assertTrue
import org.junit.Rule
import org.junit.Test

class SplashScreenVmTest {

    @get:Rule
    var liveDataRule = InstantTaskExecutorRule()

    @get:Rule
    val schedulers = RxImmediateSchedulerRule()

    private val sut = SplashScreenVM()

    @Test
    fun `Given SplashScreenVM, when PageActive event called on viewModel, then correct value should be set on viewNavigation LiveData`() {
        sut.process(Event.PageActive)

        val navigation = sut.viewNavigations().getOrAwaitValue() as NavigateToFragment
        val effect = sut.viewEffects().getOrAwaitValue() as ShowLogo

        assertTrue(sut.firstLoadOccurred)
        assertEquals(ShowLogo, effect)
        assertEquals(R.id.homeFragment, navigation.pageId)
    }
}