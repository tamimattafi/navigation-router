package com.attafitamim.navigation.router.core.navigator

import com.attafitamim.navigation.router.core.commands.Command
import com.attafitamim.navigation.router.core.global.Disposable
import com.attafitamim.navigation.router.core.handlers.ScreenBackPressHandler

interface NavigationBuffer {
    fun applyCommands(navigatorKey: String? = null, commands: Array<out Command>)
}
