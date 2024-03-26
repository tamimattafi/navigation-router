package com.attafitamim.navigation.sample.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.attafitamim.navigation.common.router.ApplicationRouter
import com.attafitamim.navigation.router.compose.navigator.ComposeNavigationDelegate
import com.attafitamim.navigation.router.compose.navigator.ComposeNavigator
import com.attafitamim.navigation.router.compose.navigator.PlatformNavigatorConfiguration
import com.attafitamim.navigation.router.compose.screens.Destination
import com.attafitamim.navigation.router.core.screens.Screen

class MainActivity : ComponentActivity(), ComposeNavigationDelegate {

    private val navigator by lazy {
        ComposeNavigator(ComposeScreenAdapter, navigationDelegate = this)
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
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            ComposeApp(navigator)
        }
    }

    override fun preformExit() {
        finish()
    }

    override fun handleExternal(screen: Screen, external: Destination.External) {
        external.forward(PlatformNavigatorConfiguration(this))
    }

    override fun onBackPressed() {
        ApplicationRouter.instance.exit()
    }
}
