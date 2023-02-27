package com.attafitamim.navigation.router.base.navigator

import com.attafitamim.navigation.router.core.commands.Command
import com.attafitamim.navigation.router.core.commands.MessageHandler
import com.attafitamim.navigation.router.core.navigator.NavigationBuffer
import com.attafitamim.navigation.router.core.navigator.Navigator
import com.attafitamim.navigation.router.core.navigator.NavigatorHolder

class SimpleNavigationBuffer(
    private val messageHandler: MessageHandler
) : NavigationBuffer, NavigatorHolder {

    private var navigators = mutableMapOf<String?, Navigator>()
    private val pendingCommands = mutableMapOf<String?, MutableList<Array<out Command>>>()

    override fun setNavigator(navigator: Navigator, navigatorKey: String?) {
        if (navigators.containsKey(navigatorKey)) {
            pendingCommands[navigatorKey]?.clear()
        }

        navigators[navigatorKey] = navigator
        pendingCommands[navigatorKey]?.apply {
            forEach(navigator::applyCommands)
            clear()
        }
    }

    override fun removeNavigator(navigatorKey: String?) {
        navigators.remove(navigatorKey)
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
}
