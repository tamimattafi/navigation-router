package com.attafitamim.navigation.router.compose.screens

import androidx.activity.ComponentActivity

actual fun interface ComposeNavigatorController : ComposeNavigatorScreen {
    fun startController(activity: ComponentActivity)
}
