@file:OptIn(ExperimentalMaterial3Api::class)

package com.attafitamim.navigation.sample.compose

import ArgumentsScreen
import MainScreen
import ResultScreen
import SimpleScreen
import androidx.compose.material3.ExperimentalMaterial3Api
import com.attafitamim.navigation.common.router.NavigationScreen
import com.attafitamim.navigation.router.compose.screens.ComposeNavigatorScreen
import com.attafitamim.navigation.router.compose.screens.ComposeScreen
import com.attafitamim.navigation.router.core.screens.Screen
import com.attafitamim.navigation.router.core.screens.platform.ScreenAdapter

expect fun toPlatformScreen(screen: NavigationScreen.TextShareDialog): ComposeNavigatorScreen

object ComposeScreenAdapter : ScreenAdapter<ComposeNavigatorScreen> {

    override fun createPlatformScreen(screen: Screen): ComposeNavigatorScreen =
        when(val navigationScreen = screen as NavigationScreen) {
            is NavigationScreen.Main -> ComposeScreen.Full {
                MainScreen()
            }

            is NavigationScreen.Simple -> ComposeScreen.Dialog.BottomSheet {
                SimpleScreen()
            }

            is NavigationScreen.Loading -> ComposeScreen.Dialog.Alert {
                TODO()
            }

            is NavigationScreen.WithArguments -> ComposeScreen.Full {
                ArgumentsScreen(navigationScreen)
            }

            is NavigationScreen.WithComplexResult -> ComposeScreen.Dialog.Alert {
                TODO()
            }

            is NavigationScreen.WithResult -> ComposeScreen.Dialog.Alert {
                ResultScreen(navigationScreen)
            }

            is NavigationScreen.PlayMarket -> {
                TODO()
            }

            is NavigationScreen.TextShareDialog -> toPlatformScreen(navigationScreen)
        }
}

