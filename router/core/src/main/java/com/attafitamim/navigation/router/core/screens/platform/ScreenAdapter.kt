package com.attafitamim.navigation.router.core.screens.platform

import com.attafitamim.navigation.router.core.screens.Screen

fun interface ScreenAdapter<T : PlatformScreen> {
    fun createPlatformScreen(screen: Screen): T
}