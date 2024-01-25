package com.attafitamim.navigation.sample.compose

import MainScreen
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

            is NavigationScreen.Simple -> ComposeScreen.Full {
                SimpleScreen()
            }

            is NavigationScreen.Loading -> ComposeScreen.Alert {
                TODO()
            }

            is NavigationScreen.WithArguments -> ComposeScreen.Full {
                TODO()
            }

            is NavigationScreen.WithComplexResult -> ComposeScreen.Alert {
                TODO()
            }

            is NavigationScreen.WithResult -> ComposeScreen.Alert {
                TODO()
            }

            is NavigationScreen.PlayMarket -> {
                TODO()
            }
        }
}

