package com.attafitamim.navigation.router.core.navigator

import com.attafitamim.navigation.router.core.commands.Command

interface NavigationBuffer : NavigatorHolder {
    fun applyCommands(navigatorKey: String? = null, commands: Array<out Command>)
}
