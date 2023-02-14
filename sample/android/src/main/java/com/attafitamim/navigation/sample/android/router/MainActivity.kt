package com.attafitamim.navigation.sample.android.router

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.attafitamim.navigation.common.router.NavigationScreen
import com.attafitamim.navigation.router.android.handlers.FragmentTransactionProcessor
import com.attafitamim.navigation.router.android.navigator.ActivityNavigator
import com.attafitamim.navigation.router.core.screens.Screen
import com.attafitamim.navigation.sample.android.R

class MainActivity : AppCompatActivity(), FragmentTransactionProcessor {

    // One time per activity (Or fragment if it has sub-fragments and plays the role of the navigator)
    private val navigator = ActivityNavigator(
        this,
        R.id.fragmentContainer,
        ScreenAdapter,
        fragmentTransactionProcessor = this
    )

    override fun onDestroy() {
        // Navigation key is optional, null is also considered as a key
        ApplicationRouter.navigatorHolder.removeNavigator()
        
        super.onDestroy()
    }

    override fun onBackPressed() {
        ApplicationRouter.instance.exit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Navigation key is optional, null is also considered as a key
        ApplicationRouter.navigatorHolder.setNavigator(navigator)

        // Start navigation from this screen
        ApplicationRouter.instance.newRootChain(NavigationScreen.Simple)
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