package com.attafitamim.navigation.router.core.handlers

import com.attafitamim.navigation.router.core.screens.Screen

fun interface ScreenExitHandler {
    fun canExitScreen(screen: Screen): Boolean
}
