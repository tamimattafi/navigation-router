package com.attafitamim.navigation.sample.android.router

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.attafitamim.navigation.common.router.NavigationScreen
import com.attafitamim.navigation.router.platform.handlers.ActivityNavigationDelegate
import com.attafitamim.navigation.router.platform.navigator.ActivityNavigator
import com.attafitamim.navigation.router.core.screens.Screen
import com.attafitamim.navigation.sample.android.R

class MainActivity : AppCompatActivity(), ActivityNavigationDelegate {

    // Better save such state in the VM to survive configuration changes
    private var isInitialized = false

    // One time per activity (Or fragment if it has sub-fragments and plays the role of the navigator)
    private val navigator by lazy {
        ActivityNavigator(
            this,
            R.id.fragmentContainer,
            ScreenAdapter,
            navigationDelegate = this
        )
    }

    override fun onResumeFragments() {
        super.onResumeFragments()

        // Navigation key is optional, null is also considered as a key
        ApplicationRouter.navigatorHolder.setNavigator(navigator)
    }

    override fun onPause() {
        // Navigation key is optional, null is also considered as a key
        ApplicationRouter.navigatorHolder.removeNavigator()

        super.onPause()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Don't attach screens if already attached, back-stack will restore them on back-press
        if (isInitialized) return
        isInitialized = true

        // Start navigation from this screen
        ApplicationRouter.instance.newRootScreen(NavigationScreen.Main)
    }

    override fun onAttachingFragment(
        transaction: FragmentTransaction,
        screen: Screen,
        isInitial: Boolean
    ) {
        // Don't add animation to the initial screen
        if (isInitial) return

        // Add animation to all screens, you can check for the screen for specific animations
        transaction.setCustomAnimations(
            R.anim.slide_in,
            R.anim.slide_out,
            R.anim.slide_in,
            R.anim.slide_out
        )
    }
}