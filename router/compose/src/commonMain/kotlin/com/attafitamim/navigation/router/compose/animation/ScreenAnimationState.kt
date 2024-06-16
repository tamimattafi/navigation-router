package com.attafitamim.navigation.router.compose.animation

import com.attafitamim.navigation.router.compose.screens.Destination

data class ScreenAnimationState(
    val screenKey: String,
    val destination: Destination.ComposeDestination
)
