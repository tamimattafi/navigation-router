package com.attafitamim.navigation.router.base.navigator

import com.attafitamim.navigation.router.core.commands.Command
import com.attafitamim.navigation.router.core.handlers.ScreenBackPressHandler
import com.attafitamim.navigation.router.core.navigator.NavigationDelegate
import com.attafitamim.navigation.router.core.navigator.Navigator
import com.attafitamim.navigation.router.core.screens.Screen

abstract class BaseNavigator(
    protected open val navigationDelegate: NavigationDelegate
) : Navigator {

    abstract val currentVisibleScreenKey: String?

    override val currentVisibleScreen: Screen? get() =
        screenHistory[currentVisibleScreenKey]

    protected open val screenHistory = mutableMapOf<String, Screen>()

    protected abstract fun exitNavigator()

    protected abstract fun releaseNavigator()

    protected abstract fun forward(screen: Screen)

    protected abstract fun replace(screen: Screen)

    protected abstract fun resetScreen(screen: Screen)

    protected abstract fun removeScreen(screen: Screen)

    /**
     * Performs [Command.BackTo] command transition
     */
    protected abstract fun backTo(screen: Screen?)

    protected abstract fun backToRoot()

    protected abstract fun setCurrentScreenBackPressHandler(handler: ScreenBackPressHandler)

    override fun applyCommands(commands: Array<out Command>) {
        for (command in commands) {
            tryApplyCommand(command)
        }
    }

    protected open fun tryApplyCommand(command: Command) {
        if (navigationDelegate.shouldApplyCommand(command)) {
            try {
                applyCommand(command)
            } catch (e: RuntimeException) {
                errorOnApplyCommand(command, e)
            }
        }
    }

    /**
     * Perform transition described by the navigation command
     *
     * @param command the navigation command to apply
     */
    protected open fun applyCommand(command: Command) {
        when (command) {
            is Command.Forward -> forward(command.screen)
            is Command.Replace -> replace(command.screen)
            is Command.BackTo -> backTo(command.screen)
            is Command.Back -> back()
            is Command.AddBackPressHandler -> setCurrentScreenBackPressHandler(command.handler)
            is Command.Remove -> remove(command.screen)
        }
    }

    protected open fun performBackPress() {
        tryApplyCommand(Command.Back)
    }

    protected open fun remove(screen: Screen) {
        if (!screenHistory.containsValue(screen)) return

        when {
            screenHistory.size > 1 || navigationDelegate.keepAfterLastScreen(screen) -> {
                removeScreen(screen)
            }

            else -> releaseNavigator()
        }
    }

    protected open fun back() {
        val visibleScreen = currentVisibleScreen

        when {
            visibleScreen == null || screenHistory.isEmpty() -> {
                releaseNavigator()
            }

            screenHistory.size > 1 || navigationDelegate.keepAfterLastScreen(visibleScreen) -> {
                removeScreen(visibleScreen)
            }

            else -> releaseNavigator()
        }
    }


    /**
     * Called when we tried to fragmentBack to some specific screen (via [Command.BackTo] command),
     * but didn't found it.
     *
     * @param screen screen
     */
    protected open fun backToUnexisting(screen: Screen) {
        backToRoot()
    }

    /**
     * Override this method if you want to handle apply command error.
     *
     * @param command command
     * @param error   error
     */
    protected open fun errorOnApplyCommand(
        command: Command,
        error: RuntimeException
    ) {
        throw error
    }
}
