package com.attafitamim.navigation.router.compose.navigator

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.IntOffset
import com.attafitamim.navigation.router.compose.screens.Destination
import com.attafitamim.navigation.router.core.commands.Command
import com.attafitamim.navigation.router.core.handlers.ScreenBackPressHandler
import com.attafitamim.navigation.router.core.navigator.Navigator
import com.attafitamim.navigation.router.core.screens.Screen
import com.attafitamim.navigation.router.core.screens.platform.ScreenAdapter

open class ComposeNavigator(
    private val screenAdapter: ScreenAdapter<Destination>,
    private val navigationDelegate: ComposeNavigationDelegate
) : Navigator {

    override val currentVisibleScreen: Screen?
        get() = currentScreenKey?.let(screens::get)

    private val screens: MutableMap<String, Screen> = mutableMapOf()
    private val composeScreens: MutableMap<String, Destination.ComposeDestination> = mutableMapOf()
    private val backHandlers: MutableMap<String, ScreenBackPressHandler> = mutableMapOf()

    private val screensStack = mutableStateOf(LinkedHashSet<String>())
    private val dialogsStack = mutableStateOf(LinkedHashSet<String>())
    private val popupsStack = mutableStateOf(LinkedHashSet<String>())
    private var lastCommand: Command? = null


    private val currentScreenKey get() =
        dialogsStack.value.lastOrNull() ?: screensStack.value.lastOrNull()

    override fun applyCommands(commands: Array<out Command>) {
        commands.forEach(::tryApplyCommand)
    }

    @Composable
    fun Content() {
        FullScreensLayout()
        DialogsLayout()
        PopupsLayout()
    }

    @Composable
    private fun QueueChangeHandler(currentQueue: LinkedHashSet<String>) {
        var previousQueue: LinkedHashSet<String> by remember {
            mutableStateOf(LinkedHashSet())
        }

        if (previousQueue != currentQueue) {
            previousQueue.forEach { key ->
                if (!currentQueue.contains(key)) {
                    savableStateHolder.current.removeState(key)
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

            val currentScreenKey = fullScreens.last()
            val currentComposeScreen = composeScreens.getValue(currentScreenKey)

            AnimatedContent(
                targetState = currentScreenKey to currentComposeScreen,
                transitionSpec = {
                    slideInHorizontally(animationSpec, initialOffset) togetherWith
                            slideOutHorizontally(animationSpec, targetOffset)
                }
            ) { pair ->
                ComposeScreenLayout(pair.first, pair.second)
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
    protected open fun ComposeScreenLayout(
        screenKey: String,
        destination: Destination.ComposeDestination
    ) {
        savableStateHolder.current.SaveableStateProvider(screenKey) {
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
            is Command.Replace -> replace(command.screen)
        }
    }

    private fun addBackHandler(handler: ScreenBackPressHandler) {
        val screenKey = currentScreenKey ?: return
        backHandlers[screenKey] = handler
    }

    private fun replaceFullScreen(screen: Screen, destination: Destination.ComposeDestination.Screen) {
        if (screensStack.value.isEmpty()) {
            forwardFullScreen(screen, destination)
            return
        }

        val currentScreen = screensStack.update {
            removeLast()
        }

        forwardFullScreen(screen, destination)
        screens.remove(currentScreen)
    }

    private fun replaceDialog(screen: Screen, destination: Destination.ComposeDestination.Dialog) {
        if (dialogsStack.value.isEmpty()) {
            forwardDialog(screen, destination)
            return
        }

        val currentScreen = dialogsStack.update {
            removeLast()
        }

        forwardDialog(screen, destination)
        screens.remove(currentScreen)
    }

    private fun replacePopup(screen: Screen, popup: Destination.ComposeDestination.Popup) {
        if (popupsStack.value.isEmpty()) {
            forwardPopup(screen, popup)
            return
        }

        val currentScreen = popupsStack.update {
            removeLast()
        }

        forwardPopup(screen, popup)
        screens.remove(currentScreen)
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
            removeElement(screenKey)
        }

        clearScreenData(screenKey)
    }

    private fun removeDialog(screenKey: String) {
        val dialogs = dialogsStack.value
        if (dialogs.contains(screenKey)) dialogsStack.update {
            removeElement(screenKey)
        }

        clearScreenData(screenKey)
    }

    private fun removeFullScreen(screenKey: String) {
        val screens = screensStack.value
        val shouldExit = screens.isEmpty() ||
                screens.last() == screenKey && screens.size == 1

        if (shouldExit) {
            exitNavigator()
            return
        }

        if (screens.contains(screenKey)) screensStack.update {
            removeElement(screenKey)
        }

        clearScreenData(screenKey)
    }

    private fun LinkedHashSet<String>.removeElement(element: String) {
        remove(element)
        clearScreenData(element)
    }

    private fun forward(screen: Screen) {
        when (val platformScreen = screenAdapter.createPlatformScreen(screen)) {
            is Destination.ComposeDestination.Dialog -> forwardDialog(screen, platformScreen)
            is Destination.ComposeDestination.Screen -> forwardFullScreen(screen, platformScreen)
            is Destination.ComposeDestination.Popup -> forwardPopup(screen, platformScreen)
            is Destination.External -> navigationDelegate.handleExternal(screen, platformScreen)
        }
    }

    private fun replace(screen: Screen) {
        when (val platformScreen = screenAdapter.createPlatformScreen(screen)) {
            is Destination.ComposeDestination.Screen -> replaceFullScreen(screen, platformScreen)
            is Destination.ComposeDestination.Dialog -> replaceDialog(screen, platformScreen)
            is Destination.ComposeDestination.Popup -> replacePopup(screen, platformScreen)
            is Destination.External -> replaceController(screen, platformScreen)
        }
    }

    private fun replaceController(screen: Screen, platformScreen: Destination.External) {
        navigationDelegate.handleExternal(screen, platformScreen)
        exitNavigator()
    }

    private fun forwardDialog(screen: Screen, dialog: Destination.ComposeDestination.Dialog) {
        val screenKey = screen.key
        val dialogs = dialogsStack.value

        if (!screens.containsKey(screenKey)) {
            screens[screenKey] = screen
            composeScreens[screenKey] = dialog
        }

        when {
            dialogs.lastOrNull() == screenKey -> return
            dialogs.contains(screenKey) -> dialogsStack.update {
                remove(screenKey)
                add(screenKey)
            }

            else -> dialogsStack.update {
                add(screenKey)
            }
        }
    }

    private fun forwardPopup(screen: Screen, popup: Destination.ComposeDestination.Popup) {
        val screenKey = screen.key
        val popups = popupsStack.value

        if (!screens.containsKey(screenKey)) {
            screens[screenKey] = screen
            composeScreens[screenKey] = popup
        }

        when {
            popups.lastOrNull() == screenKey -> return
            popups.contains(screenKey) -> popupsStack.update {
                remove(screenKey)
                add(screenKey)
            }

            else -> popupsStack.update {
                add(screenKey)
            }
        }
    }

    private fun forwardFullScreen(screen: Screen, screenScreen: Destination.ComposeDestination.Screen) {
        closeAllDialogs()

        val screenKey = screen.key
        val fullScreens = screensStack.value

        if (!screens.containsKey(screenKey)) {
            screens[screenKey] = screen
            composeScreens[screenKey] = screenScreen
        }

        when {
            fullScreens.lastOrNull() == screenKey -> return
            fullScreens.contains(screenKey) -> screensStack.update {
                remove(screenKey)
                add(screenKey)
            }

            else -> screensStack.update {
                add(screenKey)
            }
        }
    }

    private fun closeAllDialogs() {
        if (dialogsStack.value.isEmpty()) {
            return
        }

        dialogsStack.update {
            removeAll { screenKey ->
                clearScreenData(screenKey)
                true
            }
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
                val removedScreen = removeLast()
                clearScreenData(removedScreen)
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
        if (screensStack.value.isEmpty() || screensStack.value.size == 1) {
            return
        }

        navigationDelegate.onBackingToRoot()
        screensStack.update {
            remove(first())
            val firstScreen = removeFirst()

            removeAll { screenKey ->
                clearScreenData(screenKey)
                true
            }

            add(firstScreen)
        }
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

    private fun <R : Any> MutableState<LinkedHashSet<String>>.update(
        onUpdate: LinkedHashSet<String>.() -> R?
    ): R? {
        val newState = LinkedHashSet(value)
        val returnType = onUpdate.invoke(newState)
        value = newState

        return returnType
    }

    private fun <T : Any> LinkedHashSet<T>.removeFirst(): T {
        val first = first()
        remove(first)
        return first
    }

    private fun <T : Any> LinkedHashSet<T>.removeLast(): T {
        val last = last()
        remove(last)
        return last
    }

    companion object {

        val savableStateHolder: ProvidableCompositionLocal<SaveableStateHolder> =
            staticCompositionLocalOf { error("savableStateHolder not initialized") }

        @Composable
        fun Root(content: @Composable () -> Unit) {
            CompositionLocalProvider(
                savableStateHolder providesDefault rememberSaveableStateHolder(),
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
