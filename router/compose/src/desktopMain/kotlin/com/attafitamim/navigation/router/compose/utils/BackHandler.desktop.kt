package com.attafitamim.navigation.router.compose.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
actual fun BackHandler(isEnabled: Boolean, onBack: () -> Unit) {
    LaunchedEffect(isEnabled) {
        actionsStore.events.collect { action ->
            if (isEnabled && action == ActionsStore.Action.OnBackPress) {
                onBack()
            }
        }
    }
}