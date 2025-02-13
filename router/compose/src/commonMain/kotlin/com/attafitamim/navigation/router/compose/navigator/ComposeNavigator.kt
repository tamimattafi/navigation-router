package com.attafitamim.navigation.router.compose.navigator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.attafitamim.navigation.router.compose.animation.ScreenAnimation
import com.attafitamim.navigation.router.compose.animation.ScreenAnimationState
import com.attafitamim.navigation.router.compose.animation.SlideScreenAnimation
import com.attafitamim.navigation.router.compose.screens.Destination
import com.attafitamim.navigation.router.core.commands.Command
import com.attafitamim.navigation.router.core.handlers.ScreenBackPressHandler
import com.attafitamim.navigation.router.core.navigator.Navigator
import com.attafitamim.navigation.router.core.screens.Screen
import com.attafitamim.navigation.router.core.screens.platform.ScreenAdapter
import kotlinx.coroutines.flow.MutableSharedFlow

val LocalComposeNavigatorStateHolder: ProvidableCompositionLocal<SaveableStateHolder> =
    staticCompositionLocalOf { error("savableStateHolder not initialized") }

val LocalComposeNavigator: ProvidableCompositionLocal<ComposeNavigator> =
    staticCompositionLocalOf { error("savableStateHolder not initialized") }

open class ComposeNavigator(
    private val screenAdapter: ScreenAdapter<Destination>,
    private val navigationDelegate: ComposeNavigationDelegate,
    private val screenAnimation: ScreenAnimation? = SlideScreenAnimation
) : Navigator {

    override val currentVisibleScreen: Screen?
        get() = currentScreenKey?.let(screens::get)

    @Stable
    private val screens: MutableMap<String, Screen> = mutableMapOf()

    @Stable
    private val composeScreens: MutableMap<String, Destination.ComposeDestination> = mutableMapOf()

    @Stable
    private val backHandlers: MutableMap<String, ScreenBackPressHandler> = mutableMapOf()

    @Stable
    private val screensStack = mutableStateOf(ArrayDeque<String>())

    @Stable
    private val dialogsStack = mutableStateOf(ArrayDeque<String>())

    @Stable
    private val popupsStack = mutableStateOf(ArrayDeque<String>())

    @Stable
    private var lastCommand: Command? = null

    val removedScreensFlow = MutableSharedFlow<String>()
    val addedScreensFlow = MutableSharedFlow<String>()

    private val currentScreenKey get() =
        dialogsStack.value.lastOrNull() ?: screensStack.value.lastOrNull()

    override fun applyCommands(commands: Array<out Command>) {
        commands.forEach(::tryApplyCommand)
    }

    @Composable
    fun Content() {
        CompositionLocalProvider(
            LocalComposeNavigator providesDefault this,
        ) {
            FullScreensLayout()
            DialogsLayout()
            PopupsLayout()
        }
    }

    @Composable
    @Stable
    private fun QueueChangeHandler(currentQueue: ArrayDeque<String>) {
        var previousQueue: ArrayDeque<String> by remember {
            mutableStateOf(ArrayDeque())
        }

        if (previousQueue != currentQueue) {
            previousQueue.forEach { key ->
                if (!currentQueue.contains(key)) {
                    LaunchedEffect(key) {
                        removedScreensFlow.emit(key)
                    }

                    LocalComposeNavigatorStateHolder.current.removeState(key)
                    clearScreenData(key)
                }
            }

            currentQueue.forEach { key ->
                if (!previousQueue.contains(key)) {
                    LaunchedEffect(key) {
                        addedScreensFlow.emit(key)
                    }
                }
            }
        }

        previousQueue = currentQueue
    }

    @Composable
    protected open fun FullScreensLayout() {
        val fullScreens by remember { screensStack }
        QueueChangeHandler(fullScreens)

        if (!fullScreens.isEmpty()) {
            val currentScreenKey = fullScreens.last()
            val currentComposeScreen = composeScreens.getValue(currentScreenKey)

            if (screenAnimation != null) {
                screenAnimation.animate(
                    lastCommand = lastCommand,
                    state = ScreenAnimationState(
                        screenKey = currentScreenKey,
                        destination = currentComposeScreen
                    )
                ) { currentState ->
                    ComposeScreenLayout(
                        screenKey = currentState.screenKey,
                        destination = currentState.destination
                    )
                }
            } else {
                ComposeScreenLayout(
                    screenKey = currentScreenKey,
                    destination = currentComposeScreen
                )
            }
        }
    }

    @Composable
    protected open fun DialogsLayout() {
        val dialogs by remember { dialogsStack }
        QueueChangeHandler(dialogs)

        dialogs.forEach { screenKey ->
            val composeScreen = composeScreens.getValue(screenKey)
            ComposeScreenLayout(screenKey, composeScreen)
        }
    }

    @Composable
    protected open fun PopupsLayout() {
        val popups by remember { popupsStack }
        QueueChangeHandler(popups)

        popups.forEach { screenKey ->
            val composeScreen = composeScreens.getValue(screenKey)
            ComposeScreenLayout(screenKey, composeScreen)
        }
    }

    @Composable
    @Stable
    protected open fun ComposeScreenLayout(
        screenKey: String,
        destination: Destination.ComposeDestination
    ) {
        LocalComposeNavigatorStateHolder.current.SaveableStateProvider(screenKey) {
            when (destination) {
                is Destination.ComposeDestination.Dialog -> destination.Content(onDismiss = {
                    removeDialog(screenKey)
                })

                is Destination.ComposeDestination.Popup -> destination.Content(onDismiss = {
                    removePopup(screenKey)
                })

                is Destination.ComposeDestination.Screen -> {
                    destination.Content()
                }
            }
        }
    }

    protected open fun tryApplyCommand(command: Command) {
        if (navigationDelegate.shouldApplyCommand(command)) {
            try {
                applyCommand(command)
                lastCommand = command
            } catch (e: RuntimeException) {
                errorOnApplyCommand(command, e)
            }
        }
    }

    private fun applyCommand(command: Command) {
        when (command) {
            is Command.AddBackPressHandler -> addBackHandler(command.handler)
            is Command.Back -> back()
            is Command.BackTo -> backTo(command.screen)
            is Command.Forward -> forward(command.screen)
            is Command.Remove -> remove(command.screen)
            is Command.Replace -> forward(command.screen, replaceLast = true)
        }
    }

    private fun addBackHandler(handler: ScreenBackPressHandler) {
        val screenKey = currentScreenKey ?: return
        backHandlers[screenKey] = handler
    }

    private fun remove(screen: Screen) {
        val screenKey = screen.key
        if (!screens.contains(screenKey)) {
            return
        }

        when (composeScreens.getValue(screenKey)) {
            is Destination.ComposeDestination.Dialog -> removeDialog(screenKey)
            is Destination.ComposeDestination.Screen -> removeFullScreen(screenKey)
            is Destination.ComposeDestination.Popup -> removePopup(screenKey)
        }
    }

    private fun removePopup(screenKey: String) {
        val popups = popupsStack.value
        if (popups.contains(screenKey)) popupsStack.update {
            remove(screenKey)
        }
    }

    private fun removeDialog(screenKey: String) {
        val dialogs = dialogsStack.value
        if (dialogs.contains(screenKey)) dialogsStack.update {
            remove(screenKey)
        }
    }

    private fun removeFullScreen(screenKey: String) {
        val stack = screensStack.value
        val isLastScreen = stack.size == 1 && stack.last() == screenKey
        val shouldExitAfterLastScreen = isLastScreen && !keepAfterLastScreen(screenKey)
        val shouldExit = shouldExitAfterLastScreen || stack.isEmpty()

        if (shouldExit) {
            exitNavigator()
            return
        }

        if (stack.contains(screenKey)) screensStack.update {
            remove(screenKey)
        }
    }

    private fun keepAfterLastScreen(screenKey: String) =
        navigationDelegate.keepAfterLastScreen(screens.getValue(screenKey))

    private fun forward(screen: Screen, replaceLast: Boolean = false) {
        when (val platformScreen = screenAdapter.createPlatformScreen(screen)) {
            is Destination.ComposeDestination.Screen -> {
                closeAllDialogs()
                screensStack.forward(screen, platformScreen, replaceLast)
            }

            is Destination.ComposeDestination.Dialog -> {
                dialogsStack.forward(screen, platformScreen, replaceLast)
            }

            is Destination.ComposeDestination.Popup -> {
                popupsStack.forward(screen, platformScreen, replaceLast)
            }

            is Destination.External -> forwardExternal(
                screen,
                platformScreen,
                replaceLast
            )
        }
    }

    private fun forwardExternal(
        screen: Screen,
        platformScreen: Destination.External,
        replaceLast: Boolean = false
    ) {
        navigationDelegate.handleExternal(screen, platformScreen)

        if (replaceLast) {
            exitNavigator()
        }
    }

    private fun closeAllDialogs() {
        if (dialogsStack.value.isEmpty()) {
            return
        }

        dialogsStack.update {
            clear()
        }
    }

    private fun backTo(screen: Screen?) {
        val screenKey = screen?.key
        if (screen == null || !screens.contains(screenKey)) {
            backToRoot()
            return
        }

        notifyBackingToScreen(screen)
        screensStack.update {
            while (last() != screenKey) {
                removeLast()
            }
        }
    }

    private fun back() {
        val currentScreen = currentVisibleScreen ?: return exitNavigator()
        val screenKey = currentScreen.key
        val backHandler = backHandlers[screenKey]

        if (backHandler == null || backHandler.canExitScreen()) {
            backHandlers.remove(screenKey)

            if (!screens.contains(screenKey)) {
                return
            }

            when (composeScreens.getValue(screenKey)) {
                is Destination.ComposeDestination.Dialog -> removeDialog(screenKey)
                is Destination.ComposeDestination.Screen -> removeFullScreen(screenKey)
                is Destination.ComposeDestination.Popup -> return
            }
        }
    }

    private fun backToRoot() {
        navigationDelegate.onBackingToRoot()
        popupsStack.update { clear() }
        dialogsStack.update { clear() }
        screensStack.update { clear() }
    }

    private fun clearScreenData(screenKey: String) {
        screens.remove(screenKey)
        composeScreens.remove(screenKey)
    }

    private fun exitNavigator() {
        navigationDelegate.preformExit()
    }

    /**
     * Override this method if you want to handle apply command error.
     *
     * @param command command
     * @param error   error
     */
    protected open fun errorOnApplyCommand(
        command: Command,
        error: RuntimeException
    ) {
        throw error
    }

    protected open fun notifyRemovingScreen(screen: Screen) {
        val isInitial = screen.key == screensStack.value.firstOrNull()
        navigationDelegate.onRemovingScreen(screen, isInitial)
    }

    protected open fun notifyBackingToScreen(screen: Screen) {
        val isInitial = screen.key == screensStack.value.firstOrNull()
        navigationDelegate.onBackingToScreen(screen, isInitial)
    }

    private fun MutableState<ArrayDeque<String>>.forward(
        screen: Screen,
        composeScreen: Destination.ComposeDestination,
        replaceLast: Boolean = false
    ) {
        val screenKey = screen.key
        if (!screens.containsKey(screenKey)) {
            screens[screenKey] = screen
            composeScreens[screenKey] = composeScreen
        }

        forward(screenKey, replaceLast)
    }

    private fun MutableState<ArrayDeque<String>>.forward(
        screenKey: String,
        replaceLast: Boolean
    ) {
        if (value.lastOrNull() == screenKey) {
            return
        }

        update {
            if (!isEmpty() && replaceLast) {
                removeLast()
            }

            remove(screenKey)
            add(screenKey)
        }
    }

    private fun MutableState<ArrayDeque<String>>.update(
        onUpdate: ArrayDeque<String>.() -> Unit
    ) {
        val newState = ArrayDeque(value).apply(onUpdate)
        value = newState
    }

    companion object {

        @Composable
        fun Root(content: @Composable () -> Unit) {
            CompositionLocalProvider(
                LocalComposeNavigatorStateHolder providesDefault rememberSaveableStateHolder(),
            ) {
                content()
            }
        }

        @Composable
        fun Root(navigator: ComposeNavigator) = Root {
            navigator.Content()
        }
    }
}
