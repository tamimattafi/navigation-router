package com.attafitamim.navigation.sample.android.router.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.attafitamim.navigation.common.router.NavigationScreen
import com.attafitamim.navigation.router.android.handlers.FragmentNavigationDelegate
import com.attafitamim.navigation.router.android.navigator.FragmentNavigator
import com.attafitamim.navigation.router.core.screens.Screen
import com.attafitamim.navigation.sample.android.R
import com.attafitamim.navigation.sample.android.router.ApplicationRouter
import com.attafitamim.navigation.sample.android.router.ScreenAdapter

class MainFragment : Fragment(R.layout.fragment_main), FragmentNavigationDelegate {

    // Better save such state in the VM to survive configuration changes
    private var isInitialized = false

    // One time per activity (Or fragment if it has sub-fragments and plays the role of the navigator)
    private val navigator by lazy {
        FragmentNavigator(
            this,
            R.id.fragmentContainer,
            ScreenAdapter,
            navigationDelegate = this
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Don't attach screens if already attached, back-stack will restore them on back-press
        if (isInitialized) return
        isInitialized = true

        // Start navigation from this screen
        ApplicationRouter.instance.newRootScreen(
            NavigationScreen.Simple,
            NAVIGATOR_KEY
        )
    }

    override fun onResume() {
        super.onResume()

        // Navigation key is optional, null is also considered as a key
        ApplicationRouter.navigatorHolder.setNavigator(navigator, NAVIGATOR_KEY)
    }

    override fun onPause() {
        // Navigation key is optional, null is also considered as a key
        ApplicationRouter.navigatorHolder.removeNavigator(NAVIGATOR_KEY)

        super.onPause()
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

    companion object {
        // This is not the best place to save navigator keys, but it's for the sake of the example
        const val NAVIGATOR_KEY = "main"
    }
}