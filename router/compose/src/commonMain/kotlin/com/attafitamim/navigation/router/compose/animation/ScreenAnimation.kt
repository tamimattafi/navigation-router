package com.attafitamim.navigation.router.compose.animation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import com.attafitamim.navigation.router.core.commands.Command

interface ScreenAnimation {

    @Composable
    fun animate(
        lastCommand: Command?,
        state: ScreenAnimationState,
        content: @Composable AnimatedContentScope.(targetState: ScreenAnimationState) -> Unit
    )
}
