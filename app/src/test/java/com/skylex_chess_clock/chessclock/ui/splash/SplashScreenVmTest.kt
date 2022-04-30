package com.skylex_chess_clock.chessclock.ui.splash

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.SavedStateHandle
import com.skylex_chess_clock.chessclock.ui.splash.SplashScreenVM.*
import com.skylex_chess_clock.chessclock.util.getOrAwaitValue
import com.skylex_chess_clock.chessclock.R
import com.skylex_chess_clock.chessclock.ui.splash.SplashScreenVM.ViewEffect.*
import com.skylex_chess_clock.chessclock.ui.splash.SplashScreenVM.ViewNavigation.*
import com.skylex_chess_clock.chessclock.util.RxImmediateSchedulerRule
import io.mockk.mockk
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.TestScheduler
import junit.framework.TestCase.*
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.TimeUnit

class SplashScreenVmTest {

    @get:Rule
    var liveDataRule = InstantTaskExecutorRule()

    @get:Rule
    val schedulers = RxImmediateSchedulerRule()

    private val sut = SplashScreenVM()

    @Test
    fun `Given SplashScreenVM, when PageActive event called on viewModel, then correct value should be set on viewNavigation LiveData`() {

        val testScheduler = TestScheduler()
        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }

        sut.process(Event.PageActive)
        testScheduler.advanceTimeTo(3, TimeUnit.SECONDS)

        val navigation = sut.viewNavigations().getOrAwaitValue() as NavigateToFragment
        val effect = sut.viewEffects().getOrAwaitValue() as ShowLogo

        assertTrue(sut.firstLoadOccurred)
        assertEquals(ShowLogo, effect)
        assertEquals(R.id.homeFragment, navigation.pageId)
    }
}