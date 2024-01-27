package com.attafitamim.navigation.sample.compose

import ArgumentsScreen
import MainScreen
import ResultScreen
import SimpleScreen
import com.attafitamim.navigation.common.router.NavigationScreen
import com.attafitamim.navigation.router.compose.screens.ComposeScreen
import com.attafitamim.navigation.router.core.screens.Screen
import com.attafitamim.navigation.router.core.screens.platform.ScreenAdapter
object ComposeScreenAdapter : ScreenAdapter<ComposeScreen> {

    override fun createPlatformScreen(screen: Screen): ComposeScreen =
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
        }
}
