package com.attafitamim.navigation.sample.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.attafitamim.navigation.common.router.ApplicationRouter
import com.attafitamim.navigation.common.router.NavigationScreen
import com.attafitamim.navigation.router.compose.navigator.ComposeNavigator

class MainActivity : ComponentActivity() {

    private val navigator by lazy {
        ComposeNavigator(ComposeScreenAdapter)
    }

    override fun onResume() {
        super.onResume()

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

        setContent {
            navigator.Content()
        }

        // Start navigation from this screen
        ApplicationRouter.instance.newRootScreen(NavigationScreen.Main)
    }

}