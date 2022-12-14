package com.attafitamim.navigation.sample.android.router

import android.os.Handler
import android.os.Looper
import com.attafitamim.navigation.router.base.navigator.SimpleNavigationBuffer
import com.attafitamim.navigation.router.base.result.SimpleResultWire
import com.attafitamim.navigation.router.base.router.SimpleNavigationRouter
import com.attafitamim.navigation.router.core.navigator.NavigatorHolder
import com.attafitamim.navigation.router.core.router.NavigationRouter

// Better use Dependency Injection to pass router and navigatorHolder to required classes
object ApplicationRouter {

    // Call from main thread only
    val instance: NavigationRouter get() = actualInstance ?: kotlin.run {
        // Inject the router in places where you want to use it, initialize once per lifetime
        SimpleNavigationRouter(navigationBuffer, resultWire).also { router ->
            actualInstance = router
        }
    }

    // Call from main thread only
    val navigatorHolder: NavigatorHolder get() = navigationBuffer

    private var actualInstance: NavigationRouter? = null

    // Commands handler
    private val transitionHandler by lazy {
        Handler(Looper.getMainLooper())
    }

    // Initialize only once for the router
    private val resultWire by lazy {
        SimpleResultWire(transitionHandler::post)
    }

    // Initialize only once for the router
    private val navigationBuffer by lazy {
        SimpleNavigationBuffer(transitionHandler::post)
    }
}
