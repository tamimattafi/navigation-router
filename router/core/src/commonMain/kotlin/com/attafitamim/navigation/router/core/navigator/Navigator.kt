package com.attafitamim.navigation.router.core.navigator

import com.attafitamim.navigation.router.core.commands.Command
import com.attafitamim.navigation.router.core.global.Disposable
import com.attafitamim.navigation.router.core.handlers.ScreenBackPressHandler
import com.attafitamim.navigation.router.core.screens.Screen

interface Navigator {
    val currentVisibleScreen: Screen?
    fun applyCommands(commands: Array<out Command>)
}
