package com.attafitamim.navigation.router.compose.screens

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.attafitamim.navigation.router.compose.navigator.PlatformNavigatorConfiguration
import com.attafitamim.navigation.router.core.screens.platform.PlatformScreen

sealed interface Destination : PlatformScreen {

    @Stable
    sealed interface ComposeDestination : Destination {

        @Stable
        interface Screen : ComposeDestination {

            @Composable
            fun Content()

            companion object {
                operator fun invoke(
                    content: @Composable () -> Unit
                ) = object : Screen {

                    @Composable
                    override fun Content() {
                        content.invoke()
                    }
                }
            }
        }

        @Stable
        interface Dialog : ComposeDestination {

            @Composable
            fun Content(onDismiss: () -> Unit)

            @Stable
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
                        content: @Composable ColumnScope.(SheetState) -> Unit
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
                            WindowInsets.ime

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
                                contentWindowInsets = {
                                    windowInsets
                                }
                            ) {
                                content(sheetState)
                            }
                        }
                    }
                }
            }

            @Stable
            interface Alert : Dialog {

                companion object {
                    operator fun invoke(
                        dismissOnBackPress: Boolean = true,
                        dismissOnClickOutside: Boolean = true,
                        usePlatformDefaultWidth: Boolean = true,
                        content: @Composable () -> Unit
                    ) = object : Alert {

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

            @Stable
            interface Overlay : Dialog {

                companion object {
                    operator fun invoke(
                        content: @Composable (onDismiss: () -> Unit) -> Unit
                    ) = object : Overlay {

                        @Composable
                        override fun Content(onDismiss: () -> Unit) {
                            content.invoke(onDismiss)
                        }
                    }
                }
            }
        }

        @Stable
        interface Popup : ComposeDestination {

            @Composable
            fun Content(onDismiss: () -> Unit)

            companion object {
                operator fun invoke(
                    content: @Composable (onDismiss: () -> Unit) -> Unit
                ) = object : Popup {

                    @Composable
                    override fun Content(onDismiss: () -> Unit) {
                        content.invoke(onDismiss)
                    }
                }
            }
        }
    }

    fun interface External : Destination {
        fun forward(configuration: PlatformNavigatorConfiguration)
    }
}
