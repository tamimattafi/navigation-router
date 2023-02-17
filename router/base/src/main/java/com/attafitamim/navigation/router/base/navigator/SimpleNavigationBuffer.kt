package com.attafitamim.navigation.router.base.navigator

import com.attafitamim.navigation.router.core.commands.Command
import com.attafitamim.navigation.router.core.commands.MessageHandler
import com.attafitamim.navigation.router.core.global.Disposable
import com.attafitamim.navigation.router.core.handlers.CurrentScreenExitHandler
import com.attafitamim.navigation.router.core.navigator.NavigationBuffer
import com.attafitamim.navigation.router.core.navigator.Navigator
import com.attafitamim.navigation.router.core.navigator.NavigatorHolder
import com.attafitamim.navigation.router.core.screens.Screen

class SimpleNavigationBuffer(
    private val messageHandler: MessageHandler
) : NavigationBuffer, NavigatorHolder {

    private var navigators = mutableMapOf<String?, Navigator>()
    private val pendingCommands = mutableMapOf<String?, MutableList<Array<out Command>>>()
    private val screenExitHandlers = mutableMapOf<String, CurrentScreenExitHandler>()

    override fun setCurrentScreenExitHandler(
        navigatorKey: String?,
        handler: CurrentScreenExitHandler
    ): Disposable {
        if (!navigators.containsKey(navigatorKey)) {
            throw IllegalArgumentException("Navigator with the key $navigatorKey does not exists")
        }

        val visibleScreen = navigators[navigatorKey]?.currentVisibleScreen
            ?: throw IllegalArgumentException("Navigator with the key $navigatorKey does not have a visible screen")

        val exitHandlerKey = createExitHandlerKey(navigatorKey, visibleScreen)

        if (screenExitHandlers.containsKey(navigatorKey)) {
            throw IllegalArgumentException("A screen exit handler for navigator with the key $navigatorKey already exists for the current screen with the key ${visibleScreen.key}")
        }

        screenExitHandlers[exitHandlerKey] = handler
        return Disposable {
            screenExitHandlers.remove(exitHandlerKey)
        }
    }

    override fun setNavigator(navigator: Navigator, navigatorKey: String?) {
        if (navigators.containsKey(navigatorKey)) {
            pendingCommands[navigatorKey]?.clear()
        }

        navigator.setScreenExitCallbackHandler { screen ->
            canScreenBackPress(navigatorKey, screen)
        }

        navigators[navigatorKey] = navigator
        pendingCommands[navigatorKey]?.apply {
            forEach(navigator::applyCommands)
            clear()
        }
    }

    override fun removeNavigator(navigatorKey: String?) {
        val navigator = navigators.remove(navigatorKey)
        navigator?.removeScreenExitCallbackHandler()
    }

    override fun applyCommands(navigatorKey: String?, commands: Array<out Command>) {
        messageHandler.post {
            val navigator = navigators[navigatorKey]
            navigator?.applyCommands(commands) ?: provideCommandsList(navigatorKey).add(commands)
        }
    }

    private fun provideCommandsList(navigatorKey: String?): MutableList<Array<out Command>> =
        pendingCommands[navigatorKey] ?: mutableListOf<Array<out Command>>().also { list ->
            pendingCommands[navigatorKey] = list
        }

    private fun canScreenBackPress(navigatorKey: String?, screen: Screen): Boolean {
        val exitHandlerKey = createExitHandlerKey(navigatorKey, screen)
        val screenHandler = screenExitHandlers[exitHandlerKey]
        val canScreenExit = screenHandler?.canExitScreen()
        return canScreenExit ?: true
    }

    private fun createExitHandlerKey(navigatorKey: String?, screen: Screen) = buildString {
        append(navigatorKey, screen.key)
    }
}
