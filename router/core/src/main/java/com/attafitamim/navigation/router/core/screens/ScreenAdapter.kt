package com.attafitamim.navigation.router.core.screens

fun interface ScreenAdapter<T : Screen> {
    fun createPlatformScreen(screen: Screen): T
}