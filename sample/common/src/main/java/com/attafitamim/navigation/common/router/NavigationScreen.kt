package com.attafitamim.navigation.common.router

import com.attafitamim.navigation.router.core.screens.Screen

sealed interface NavigationScreen : Screen {

    object Simple : NavigationScreen

    data class WithArguments(
        val arg1: String,
        val arg2: String
    ) : NavigationScreen

    data class WithResult(
        val arg1: String
    ) : NavigationScreen

    data class WithComplexResult(
        val arg1: String
    ) : NavigationScreen
}
