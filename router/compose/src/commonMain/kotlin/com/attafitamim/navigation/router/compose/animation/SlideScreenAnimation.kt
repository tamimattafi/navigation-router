package com.attafitamim.navigation.router.compose.animation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset
import com.attafitamim.navigation.router.core.commands.Command

object SlideScreenAnimation : ScreenAnimation {

    @Composable
    override fun animate(
        lastCommand: Command?,
        state: ScreenAnimationState,
        content: @Composable AnimatedContentScope.(targetState: ScreenAnimationState) -> Unit
    ) {
        val animationSpec: FiniteAnimationSpec<IntOffset> = spring(
            stiffness = Spring.StiffnessMediumLow,
            visibilityThreshold = IntOffset.VisibilityThreshold
        )

        val (initialOffset, targetOffset) = when (lastCommand) {
            is Command.Back,
            is Command.BackTo,
            is Command.Remove -> ({ size: Int -> -size }) to ({ size: Int -> size })

            else -> ({ size: Int -> size }) to ({ size: Int -> -size })
        }

        AnimatedContent(
            targetState = state,
            transitionSpec = {
                slideInHorizontally(animationSpec, initialOffset) togetherWith
                    slideOutHorizontally(animationSpec, targetOffset)
            },
            content = content
        )
    }
}
