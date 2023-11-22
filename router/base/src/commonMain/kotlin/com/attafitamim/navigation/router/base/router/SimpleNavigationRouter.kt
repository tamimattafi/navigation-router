package com.attafitamim.navigation.router.base.router

import com.attafitamim.navigation.router.core.commands.Command
import com.attafitamim.navigation.router.core.handlers.ScreenBackPressHandler
import com.attafitamim.navigation.router.core.navigator.NavigationBuffer
import com.attafitamim.navigation.router.core.result.ResultWire
import com.attafitamim.navigation.router.core.router.NavigationRouter
import com.attafitamim.navigation.router.core.screens.Screen

/**
 * Router is the class for high-level navigation.
 *
 * Use it to perform needed transitions.
 * This implementation covers almost all cases needed for the average app.
 * Extend it if you need some tricky navigation.
 */
open class SimpleNavigationRouter(
    navigationBuffer: NavigationBuffer,
    resultWire: ResultWire
) : BaseRouter(navigationBuffer, resultWire), NavigationRouter {

    override fun navigateTo(screen: Screen, navigatorKey: String?) {
        executeCommands(
            navigatorKey,
            Command.Forward(screen)
        )
    }

    override fun newRootScreen(screen: Screen, navigatorKey: String?) {
        executeCommands(
            navigatorKey,
            Command.BackTo(null),
            Command.Replace(screen)
        )
    }

    override fun replaceScreen(screen: Screen, navigatorKey: String?) {
        executeCommands(
            navigatorKey,
            Command.Replace(screen)
        )
    }

    override fun removeScreen(screen: Screen, navigatorKey: String?) {
        executeCommands(
            navigatorKey,
            Command.Remove(screen)
        )
    }

    override fun backTo(screen: Screen?, navigatorKey: String?) {
        executeCommands(
            navigatorKey,
            Command.BackTo(screen)
        )
    }

    override fun newChain(vararg screens: Screen, navigatorKey: String?) {
        val commands = screens.map { screen ->
            Command.Forward(screen)
        }

        executeCommands(navigatorKey, *commands.toTypedArray())
    }

    override fun newRootChain(vararg screens: Screen, navigatorKey: String?) {
        val commands = screens.mapIndexed { index, screen ->
            if (index == 0) Command.Replace(screen)
            else Command.Forward(screen)
        }

        executeCommands(navigatorKey, Command.BackTo(null), *commands.toTypedArray())
    }

    override fun finishChain(navigatorKey: String?) {
        executeCommands(navigatorKey, Command.BackTo(null), Command.Back)
    }

    override fun exit(navigatorKey: String?) {
        executeCommands(navigatorKey, Command.Back)
    }

    override fun setCurrentScreenExitHandler(
        navigatorKey: String?,
        handler: ScreenBackPressHandler
    ) {
        executeCommands(navigatorKey, Command.AddBackPressHandler(handler))
    }
}
