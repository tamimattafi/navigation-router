package com.attafitamim.navigation.sample.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.attafitamim.navigation.common.router.ApplicationRouter
import com.attafitamim.navigation.common.router.NavigationScreen
import com.attafitamim.navigation.router.compose.navigator.ComposeNavigator

@Composable
fun ComposeApp(navigator: ComposeNavigator) = ComposeNavigator.Root {
    LaunchedEffect(navigator) {
        ApplicationRouter.instance.newRootScreen(NavigationScreen.Main)
    }

    MaterialTheme {
        Box(Modifier.safeDrawingPadding()) {
            navigator.Content()
        }
    }
}