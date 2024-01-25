package com.attafitamim.navigation.router.core.navigator

import com.attafitamim.navigation.router.core.commands.Command
import com.attafitamim.navigation.router.core.screens.Screen

interface NavigationDelegate {

    fun shouldApplyCommand(command: Command): Boolean = true

    fun onRemovingScreen(
        screen: Screen,
        isInitial: Boolean
    ) {}

    fun onBackingToScreen(
        screen: Screen,
        isInitial: Boolean
    ) {}

    fun onBackingToRoot() {}

    fun keepAfterLastScreen(lastScreen: Screen): Boolean = false
}
