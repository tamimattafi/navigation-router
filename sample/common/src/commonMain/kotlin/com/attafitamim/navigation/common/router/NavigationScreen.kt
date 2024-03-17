package com.attafitamim.navigation.common.router

import com.attafitamim.navigation.router.core.screens.Screen

sealed interface NavigationScreen : Screen {

    data object Main : NavigationScreen

    data object Simple : NavigationScreen

    data object Loading : NavigationScreen

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

    data object PlayMarket : NavigationScreen

    data class TextShareDialog(
        val text: String
    ) : NavigationScreen
}
