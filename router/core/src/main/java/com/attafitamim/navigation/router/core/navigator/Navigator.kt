package com.attafitamim.navigation.router.core.navigator

import com.attafitamim.navigation.router.core.commands.Command
import com.attafitamim.navigation.router.core.screens.Screen

interface Navigator {
    fun applyCommands(commands: Array<out Command>)
}
