package com.attafitamim.navigation.router.compose.utils

import androidx.compose.runtime.Composable
import androidx.activity.compose.BackHandler as PlatformBackHandler
@Composable
actual fun BackHandler(isEnabled: Boolean, onBack: () -> Unit) {
    PlatformBackHandler(isEnabled, onBack)
}
