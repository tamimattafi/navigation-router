package com.attafitamim.navigation.router.compose.navigator

import com.attafitamim.navigation.router.compose.screens.Destination
import com.attafitamim.navigation.router.core.navigator.NavigationDelegate
import com.attafitamim.navigation.router.core.screens.Screen

interface ComposeNavigationDelegate : NavigationDelegate {

    fun preformExit()

    fun handleExternal(screen: Screen, external: Destination.External)
}
