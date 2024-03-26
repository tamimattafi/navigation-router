@file:OptIn(ExperimentalMaterial3Api::class)

package com.attafitamim.navigation.sample.compose

import ArgumentsScreen
import MainScreen
import ResultScreen
import SimpleScreen
import SnackBarLayout
import androidx.compose.material3.ExperimentalMaterial3Api
import com.attafitamim.navigation.common.router.NavigationScreen
import com.attafitamim.navigation.router.compose.screens.Destination
import com.attafitamim.navigation.router.core.screens.Screen
import com.attafitamim.navigation.router.core.screens.platform.ScreenAdapter

expect fun toPlatformScreen(screen: NavigationScreen.TextShareDialog): Destination

object ComposeScreenAdapter : ScreenAdapter<Destination> {

    override fun createPlatformScreen(screen: Screen): Destination =
        when(val navigationScreen = screen as NavigationScreen) {
            is NavigationScreen.Main -> Destination.ComposeDestination.Screen {
                MainScreen()
            }

            is NavigationScreen.Simple -> Destination.ComposeDestination.Dialog.BottomSheet {
                SimpleScreen()
            }

            is NavigationScreen.Loading -> Destination.ComposeDestination.Dialog.Alert {
                TODO()
            }

            is NavigationScreen.WithArguments -> Destination.ComposeDestination.Screen {
                ArgumentsScreen(navigationScreen)
            }

            is NavigationScreen.WithComplexResult -> Destination.ComposeDestination.Dialog.Alert {
                TODO()
            }

            is NavigationScreen.WithResult -> Destination.ComposeDestination.Dialog.Alert {
                ResultScreen(navigationScreen)
            }

            is NavigationScreen.PlayMarket -> {
                TODO()
            }

            is NavigationScreen.TextShareDialog -> toPlatformScreen(navigationScreen)

            is NavigationScreen.SnackBar -> Destination.ComposeDestination.Popup { onDismiss ->
                SnackBarLayout(navigationScreen.text, onDismiss)
            }
        }
}

