package com.attafitamim.navigation.router.compose.screens

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
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

                @OptIn(ExperimentalMaterial3Api::class)
                operator fun invoke(
                    modifier: Modifier = Modifier,
                    overrideSheetState: SheetState? = null,
                    overrideShape: Shape? = null,
                    overrideContainerColor: Color? = null,
                    overrideContentColor: Color? = null,
                    overrideTonalElevation: Dp? = null,
                    overrideScrimColor: Color? = null,
                    overrideDragHandle: @Composable (() -> Unit)? = null,
                    overrideWindowInsets: WindowInsets? = null,
                    content: @Composable ColumnScope.() -> Unit
                ) = object : BottomSheet {

                    @Composable
                    override fun Content(onDismiss: () -> Unit) {
                        val sheetState = overrideSheetState ?:
                            rememberModalBottomSheetState(
                                skipPartiallyExpanded = true,
                                confirmValueChange = { sheetValue ->
                                    sheetValue != SheetValue.PartiallyExpanded
                                }
                            )

                        val shape = overrideShape ?:
                            BottomSheetDefaults.ExpandedShape

                        val containerColor = overrideContainerColor
                            ?: BottomSheetDefaults.ContainerColor

                        val contentColor = overrideContentColor
                            ?: contentColorFor(containerColor)

                        val tonalElevation = overrideTonalElevation ?:
                            BottomSheetDefaults.Elevation

                        val scrimColor = overrideScrimColor ?:
                            BottomSheetDefaults.ScrimColor

                        val dragHandle = overrideDragHandle ?:
                            { BottomSheetDefaults.DragHandle() }

                        val windowInsets = overrideWindowInsets ?:
                            BottomSheetDefaults.windowInsets

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

        interface Overlay : Dialog {

            companion object {
                operator fun invoke(
                    content: @Composable (onDismiss: () -> Unit) -> Unit
                ) = object : Dialog {

                    @Composable
                    override fun Content(onDismiss: () -> Unit) {
                        content.invoke(onDismiss)
                    }
                }
            }
        }
    }
}
