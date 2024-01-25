package com.attafitamim.navigation.router.compose.navigator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.attafitamim.navigation.router.compose.screens.ComposeScreen
import com.attafitamim.navigation.router.core.commands.Command
import com.attafitamim.navigation.router.core.handlers.ScreenBackPressHandler
import com.attafitamim.navigation.router.core.navigator.NavigationDelegate
import com.attafitamim.navigation.router.core.navigator.Navigator
import com.attafitamim.navigation.router.core.screens.Screen
import com.attafitamim.navigation.router.core.screens.platform.ScreenAdapter

open class ComposeNavigator(
    private val screenAdapter: ScreenAdapter<ComposeScreen>,
    private val navigationDelegate: NavigationDelegate
) : Navigator {

    private val currentScreenKey = mutableStateOf<String?>(null)
    private val screens: MutableMap<String, Screen> = mutableMapOf()
    private val screensQueue = ArrayDeque<String>()

    override val currentVisibleScreen: Screen?
        get() = screens[currentScreenKey.value]

    override fun applyCommands(commands: Array<out Command>) {
        commands.forEach(::tryApplyCommand)
    }

    @Composable
    fun Content() {
        val currentScreenKey by remember { currentScreenKey }
        val screen = screens[currentScreenKey]

        if (screen != null) {
            val platformScreen = screenAdapter.createPlatformScreen(screen)
            platformScreen.Content()
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

    private fun applyCommand(command: Command) {
        when (command) {
            is Command.AddBackPressHandler -> addBackHandler(command.handler)
            is Command.Back -> back()
            is Command.BackTo -> backTo(command.screen)
            is Command.Forward -> forward(command.screen)
            is Command.Remove -> remove(command.screen)
            is Command.Replace -> replace(command.screen)
        }
    }

    private fun addBackHandler(handler: ScreenBackPressHandler) {
        // handler.canExitScreen()
    }

    private fun replace(screen: Screen) {
        if (screensQueue.isEmpty()) {
            forward(screen)
            return
        }

        val currentScreen = screensQueue.removeLast()
        forward(screen)
        screens.remove(currentScreen)
    }

    private fun remove(screen: Screen) {
        val screenKey = screen.key

        if (!screens.contains(screenKey)) {
            return
        }

        notifyRemovingScreen(screen)
        if (screensQueue.last() == screenKey) {
            removeLast()
            return
        }

        val screensToKeep = ArrayList<String>(screensQueue.size)
        while (screensQueue.last() != screenKey) {
            val removedScreenKey = screensQueue.removeLast()
            screensToKeep.add(removedScreenKey)
        }

        val screenToRemove = screensQueue.removeLast()
        screens.remove(screenToRemove)

        screensQueue.addAll(screensToKeep)
    }

    private fun forward(screen: Screen) {
        val screenKey = screen.key
        screensQueue.addLast(screenKey)
        screens[screenKey] = screen
        currentScreenKey.value = screenKey
    }

    private fun backTo(screen: Screen?) {
        val screenKey = screen?.key
        if (screen == null || !screens.contains(screenKey)) {
            backToRoot()
            return
        }

        notifyBackingToScreen(screen)
        currentScreenKey.value = screenKey
        while (screensQueue.last() != screenKey) {
            val removedScreen = screensQueue.removeLast()
            screens.remove(removedScreen)
        }
    }

    private fun back() {
        if (screensQueue.isEmpty() || screensQueue.size == 1) {
            exitNavigator()
            return
        }

        removeLast()
    }

    private fun removeLast() {
        val currentScreen = screensQueue.removeLast()
        val previousScreen = screensQueue.last()
        currentScreenKey.value = previousScreen
        screens.remove(currentScreen)
    }

    private fun backToRoot() {
        if (screensQueue.isEmpty() || screensQueue.size == 1) {
            return
        }

        navigationDelegate.onBackingToRoot()
        val firstScreen = screensQueue.removeFirst()
        currentScreenKey.value = firstScreen

        screensQueue.forEach { screen ->
            screens.remove(screen)
        }

        screensQueue.clear()
        screensQueue.addLast(firstScreen)
    }

    private fun releaseNavigator() {
        currentScreenKey.value = null
        screensQueue.clear()
        screens.clear()
    }

    private fun exitNavigator() {
        releaseNavigator()
        // TODO: call delegate exist
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

    protected open fun notifyRemovingScreen(screen: Screen) {
        val isInitial = screen.key == screensQueue.firstOrNull()
        navigationDelegate.onRemovingScreen(screen, isInitial)
    }

    protected open fun notifyBackingToScreen(screen: Screen) {
        val isInitial = screen.key == screensQueue.firstOrNull()
        navigationDelegate.onBackingToScreen(screen, isInitial)
    }
}
