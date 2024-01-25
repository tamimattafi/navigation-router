package com.attafitamim.navigation.router.compose.screens

import androidx.compose.runtime.Composable
import com.attafitamim.navigation.router.core.screens.platform.PlatformScreen

sealed interface ComposeScreen : PlatformScreen {

    @Composable
    fun Content()

    interface Sheet : ComposeScreen {
        companion object {
            operator fun invoke(
                content: @Composable () -> Unit
            ) = object : Sheet {

                @Composable
                override fun Content() {
                    content.invoke()
                }
            }
        }
    }

    interface Alert : ComposeScreen {
        companion object {
            operator fun invoke(
                content: @Composable () -> Unit
            ) = object : Alert {

                @Composable
                override fun Content() {
                    content.invoke()
                }
            }
        }
    }

    interface Full : ComposeScreen {
        companion object {
            operator fun invoke(
                content: @Composable () -> Unit
            ) = object : Full {

                @Composable
                override fun Content() {
                    content.invoke()
                }
            }
        }
    }
}
