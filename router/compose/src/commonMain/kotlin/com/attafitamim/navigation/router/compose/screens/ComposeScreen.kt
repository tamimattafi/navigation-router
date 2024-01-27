@file:OptIn(ExperimentalMaterial3Api::class)

package com.attafitamim.navigation.router.compose.screens

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.attafitamim.navigation.router.core.screens.platform.PlatformScreen

sealed interface ComposeScreen : PlatformScreen {

    interface Full : ComposeScreen {

        @Composable
        fun Content()

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

    interface Dialog : ComposeScreen {

        @Composable
        fun Content(onDismiss: () -> Unit)

        interface BottomSheet : Dialog {

            companion object {
                operator fun invoke(
                    modifier: Modifier,
                    sheetState: SheetState,
                    shape: Shape,
                    containerColor: Color,
                    contentColor: Color,
                    tonalElevation: Dp,
                    scrimColor: Color,
                    dragHandle: @Composable (() -> Unit)?,
                    windowInsets: WindowInsets,
                    content: @Composable ColumnScope.() -> Unit
                ) = object : BottomSheet {

                    @Composable
                    override fun Content(onDismiss: () -> Unit) {
                        ModalBottomSheet(
                            onDismissRequest = onDismiss,
                            modifier = modifier,
                            sheetState = sheetState,
                            shape = shape,
                            containerColor = containerColor,
                            contentColor = contentColor,
                            tonalElevation = tonalElevation,
                            scrimColor = scrimColor,
                            dragHandle = dragHandle,
                            windowInsets = windowInsets,
                            content = content
                        )
                    }
                }

                operator fun invoke(
                    modifier: Modifier = Modifier,
                    overrideContainerColor: Color? = null,
                    overrideContentColor: Color? = null,
                    content: @Composable ColumnScope.() -> Unit
                ) = object : BottomSheet {

                    @Composable
                    override fun Content(onDismiss: () -> Unit) {
                        val sheetState = rememberModalBottomSheetState(
                            skipPartiallyExpanded = true
                        )

                        val containerColor = overrideContainerColor
                            ?: BottomSheetDefaults.ContainerColor

                        val contentColor = overrideContentColor
                            ?: contentColorFor(containerColor)

                        ModalBottomSheet(
                            onDismissRequest = onDismiss,
                            modifier = modifier,
                            sheetState = sheetState,
                            shape = BottomSheetDefaults.ExpandedShape,
                            containerColor = containerColor,
                            contentColor = contentColor,
                            tonalElevation = BottomSheetDefaults.Elevation,
                            scrimColor = BottomSheetDefaults.ScrimColor,
                            dragHandle = null,
                            windowInsets = BottomSheetDefaults.windowInsets,
                            content = content
                        )
                    }
                }
            }
        }

        interface Alert : Dialog {

            companion object {
                operator fun invoke(
                    dismissOnBackPress: Boolean = true,
                    dismissOnClickOutside: Boolean = true,
                    usePlatformDefaultWidth: Boolean = true,
                    content: @Composable () -> Unit
                ) = object : Dialog {

                    @Composable
                    override fun Content(onDismiss: () -> Unit) {
                        val properties = remember {
                            DialogProperties(
                                dismissOnBackPress = dismissOnBackPress,
                                dismissOnClickOutside = dismissOnClickOutside,
                                usePlatformDefaultWidth = usePlatformDefaultWidth
                            )
                        }

                        Dialog(
                            properties = properties,
                            onDismissRequest = onDismiss,
                        ) {
                            content.invoke()
                        }
                    }
                }
            }
        }

    }
}
