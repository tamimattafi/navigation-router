package com.attafitamim.navigation.common.router

import com.attafitamim.navigation.router.base.navigator.SimpleNavigationBuffer
import com.attafitamim.navigation.router.base.result.SimpleResultWire
import com.attafitamim.navigation.router.base.router.SimpleNavigationRouter
import com.attafitamim.navigation.router.core.commands.MessageHandler
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

    // Initialize only once for the router
    private val messageHandler by lazy {
        MessageHandler { action ->
            // Maybe ensure that the call is on main thread
            action.invoke()
        }
    }

    // Initialize only once for the router
    private val resultWire by lazy {
        SimpleResultWire(messageHandler)
    }

    // Initialize only once for the router
    private val navigationBuffer by lazy {
        SimpleNavigationBuffer(messageHandler)
    }
}
