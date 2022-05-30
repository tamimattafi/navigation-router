package com.attafitamim.navigation.router.core.commands

import com.attafitamim.navigation.router.core.screens.Screen

sealed interface Command {
    object Back : Command
    data class Forward(val screen: Screen) : Command
    data class Replace(val screen: Screen) : Command
    data class BackTo(val screen: Screen?) : Command
}
