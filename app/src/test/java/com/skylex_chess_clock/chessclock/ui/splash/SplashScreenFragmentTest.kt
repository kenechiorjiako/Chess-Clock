package com.skylex_chess_clock.chessclock.ui.splash

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.skylex_chess_clock.chessclock.R
import com.skylex_chess_clock.chessclock.util.RxImmediateSchedulerRule
import com.skylex_chess_clock.chessclock.util.launch
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.TestScheduler
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.concurrent.TimeUnit


@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(application = HiltTestApplication::class)
class SplashScreenFragmentTest {

    @get:Rule
    val hiltAndroidRule = HiltAndroidRule(this)

    @get:Rule
    val liveDataRule = InstantTaskExecutorRule()

    @get:Rule
    val rxJavaRule = RxImmediateSchedulerRule()

    private val sut = SplashScreenFragment()

    @Before
    fun setup() {

    }

    @Test
    fun `Given splash screen fragment, when navigate to home fragment called, then page should navigate`() {
        val testScheduler = TestScheduler()
        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }

        launch(sut) {
            val testNavController = TestNavHostController(it)
            testNavController.setGraph(R.navigation.main_navigation_graph)
            Navigation.setViewNavController(sut.requireView(), testNavController)

            testScheduler.advanceTimeBy(10, TimeUnit.SECONDS)
            assertNotNull(testNavController.currentDestination)
            assertEquals(R.id.homeFragment, testNavController.currentDestination?.id)
        }
    }
}