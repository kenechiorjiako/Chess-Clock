package com.skylex_chess_clock.chessclock.ui.splash

import android.animation.AnimatorSet
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.skylex_chess_clock.chessclock.R
import com.skylex_chess_clock.chessclock.databinding.FragmentSplashScreenBinding
import com.skylex_chess_clock.chessclock.util.AnimationUtil
import com.skylex_chess_clock.chessclock.ui.splash.SplashScreenVM.*
import com.skylex_chess_clock.chessclock.ui.splash.SplashScreenVM.ViewNavigation.*
import com.skylex_chess_clock.news_feed.util.MviFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashScreenFragment() : MviFragment<Any, ViewEffect, ViewNavigation, Event, Any, SplashScreenVM>() {

    lateinit var binding: FragmentSplashScreenBinding
    override val viewModel: SplashScreenVM by viewModels()
    lateinit var navController: NavController
    private val animationUtil: AnimationUtil = AnimationUtil()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSplashScreenBinding.inflate(inflater, container, false)

        return binding.root
    }


    override fun setupViewHelperObjects() {
        navController = NavHostFragment.findNavController(this)
    }
    override fun setupViews() {
    }
    override fun setupViewListeners() {
    }

    override fun renderViewState(viewState: Any) {
    }

    override fun renderViewEffect(viewEffect: ViewEffect) {

    }
    private fun showLogo() {
        val objectAnimator = animationUtil.fadeIn(binding.logo, 2000, 0f, 1f)
        val objectAnimator2 = animationUtil.fadeIn(binding.name, 2000, 0f, 1f)
        val animatorSet = AnimatorSet()
        animatorSet.apply {
            animatorSet.startDelay = 200
            animatorSet.playTogether(objectAnimator, objectAnimator2)
            start()
        }
    }

    override fun handleViewNavigation(viewNavigation: ViewNavigation) {
        when(viewNavigation) {
            is NavigateToFragment -> navigateToFragment(viewNavigation.pageId)
        }
    }
    private fun navigateToFragment(pageId: Int) {
        val navOptions: NavOptions = NavOptions.Builder()
                .setPopUpTo(R.id.splashScreenFragment, true)
                .setEnterAnim(android.R.anim.fade_in)
                .setExitAnim(android.R.anim.fade_out)
                .build()
        navController.navigate(pageId, null, navOptions)
    }


    override fun dispatchLoadPageEvent() {
        mEvents.onNext(Event.PageActive)
    }

    companion object {
        private const val TAG = "SplashScreenFragment"
    }
}