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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.IntOffset
import com.attafitamim.navigation.router.compose.screens.ComposeNavigatorController
import com.attafitamim.navigation.router.compose.screens.ComposeNavigatorScreen
import com.attafitamim.navigation.router.compose.screens.ComposeScreen
import com.attafitamim.navigation.router.compose.utils.BackHandler
import com.attafitamim.navigation.router.core.commands.Command
import com.attafitamim.navigation.router.core.handlers.ScreenBackPressHandler
import com.attafitamim.navigation.router.core.navigator.Navigator
import com.attafitamim.navigation.router.core.screens.Screen
import com.attafitamim.navigation.router.core.screens.platform.ScreenAdapter

open class ComposeNavigator(
    private val screenAdapter: ScreenAdapter<ComposeNavigatorScreen>,
    private val navigationDelegate: ComposeNavigationDelegate
) : Navigator {

    override val currentVisibleScreen: Screen?
        get() = currentScreenKey?.let(screens::get)

    private val screens: MutableMap<String, Screen> = mutableMapOf()
    private val composeScreens: MutableMap<String, ComposeScreen> = mutableMapOf()
    private val backHandlers: MutableMap<String, ScreenBackPressHandler> = mutableMapOf()

    private val fullScreensQueue = mutableStateOf(ArrayDeque<String>())
    private val dialogsQueue = mutableStateOf(ArrayDeque<String>())
    private var lastCommand: Command? = null

    private val currentScreenKey get() =
        dialogsQueue.value.lastOrNull() ?: fullScreensQueue.value.lastOrNull()

    override fun applyCommands(commands: Array<out Command>) {
        commands.forEach(::tryApplyCommand)
    }

    @Composable
    fun Content() {
        BackHandler(isEnabled = true) {
            back()
        }

        FullScreensLayout()
        DialogsLayout()
    }

    @Composable
    protected open fun FullScreensLayout() {
        val fullScreens by remember { fullScreensQueue }

        if (!fullScreens.isEmpty()) {
            for (screenPosition in 0 until fullScreens.lastIndex) {
                val screenKey = fullScreens[screenPosition]
                val composeScreen = composeScreens.getValue(screenKey)
                ComposeScreenLayout(screenKey, composeScreen)
            }

            val currentScreenKey = fullScreens.last()
            val currentComposeScreen = composeScreens.getValue(currentScreenKey)

            val (initialOffset, targetOffset) = when (lastCommand) {
                is Command.Back,
                is Command.BackTo,
                is Command.Remove -> ({ size: Int -> -size }) to ({ size: Int -> size })
                else -> ({ size: Int -> size }) to ({ size: Int -> -size })
            }

            val animationSpec: FiniteAnimationSpec<IntOffset> = spring(
                stiffness = Spring.StiffnessMediumLow,
                visibilityThreshold = IntOffset.VisibilityThreshold
            )

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
        val dialogScreens by remember { dialogsQueue }
        dialogScreens.forEach { screenKey ->
            val composeScreen = composeScreens.getValue(screenKey)
            ComposeScreenLayout(screenKey, composeScreen)
        }
    }

    @Composable
    protected open fun ComposeScreenLayout(screenKey: String, composeScreen: ComposeScreen) {
        when (composeScreen) {
            is ComposeScreen.Dialog -> composeScreen.Content(onDismiss = {
                dialogsQueue.update {
                    clearScreenData(screenKey)
                    remove(screenKey)
                }
            })

            is ComposeScreen.Full -> {
                composeScreen.Content()
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

    private fun replaceFullScreen(screen: Screen, composeScreen: ComposeScreen.Full) {
        if (fullScreensQueue.value.isEmpty()) {
            forwardFullScreen(screen, composeScreen)
            return
        }

        val currentScreen = fullScreensQueue.update {
            removeLast()
        }

        forwardFullScreen(screen, composeScreen)
        screens.remove(currentScreen)
    }

    private fun replaceDialog(screen: Screen, composeScreen: ComposeScreen.Dialog) {
        if (dialogsQueue.value.isEmpty()) {
            forwardDialog(screen, composeScreen)
            return
        }

        val currentScreen = dialogsQueue.update {
            removeLast()
        }

        forwardDialog(screen, composeScreen)
        screens.remove(currentScreen)
    }

    private fun remove(screen: Screen) {
        val screenKey = screen.key
        if (!screens.contains(screenKey)) {
            return
        }

        when (composeScreens.getValue(screenKey)) {
            is ComposeScreen.Dialog -> removeDialog(screenKey)
            is ComposeScreen.Full -> removeFullScreen(screenKey)
        }
    }

    private fun removeDialog(screenKey: String) {
        val dialogs = dialogsQueue.value
        if (dialogs.contains(screenKey)) dialogsQueue.update {
            removeElement(screenKey)
        }

        clearScreenData(screenKey)
    }

    private fun removeFullScreen(screenKey: String) {
        val screens = fullScreensQueue.value
        val shouldExit = screens.isEmpty() ||
                screens.last() == screenKey && screens.size == 1

        if (shouldExit) {
            exitNavigator()
            return
        }

        if (screens.contains(screenKey)) fullScreensQueue.update {
            removeElement(screenKey)
        }

        clearScreenData(screenKey)
    }

    private fun ArrayDeque<String>.removeElement(element: String) {
        val elementsToKeep = ArrayList<String>(size)
        while (last() != element) {
            val removedElement = removeLast()
            elementsToKeep.add(removedElement)
        }

        val screenKey = removeLast()
        clearScreenData(screenKey)

        addAll(elementsToKeep)
    }

    private fun forward(screen: Screen) {
        when (val platformScreen = screenAdapter.createPlatformScreen(screen)) {
            is ComposeScreen.Dialog -> forwardDialog(screen, platformScreen)
            is ComposeScreen.Full -> forwardFullScreen(screen, platformScreen)
            is ComposeNavigatorController -> navigationDelegate.forwardController(screen, platformScreen)
        }
    }

    private fun replace(screen: Screen) {
        when (val platformScreen = screenAdapter.createPlatformScreen(screen)) {
            is ComposeScreen.Full -> replaceFullScreen(screen, platformScreen)
            is ComposeScreen.Dialog -> replaceDialog(screen, platformScreen)
            is ComposeNavigatorController -> replaceController(screen, platformScreen)
        }
    }

    private fun replaceController(screen: Screen, platformScreen: ComposeNavigatorController) {
        navigationDelegate.forwardController(screen, platformScreen)
        exitNavigator()
    }

    private fun forwardDialog(screen: Screen, dialog: ComposeScreen.Dialog) {
        val screenKey = screen.key
        val dialogs = dialogsQueue.value

        if (!screens.containsKey(screenKey)) {
            screens[screenKey] = screen
            composeScreens[screenKey] = dialog
        }

        when {
            dialogs.lastOrNull() == screenKey -> return
            dialogs.contains(screenKey) -> dialogsQueue.update {
                remove(screenKey)
                addLast(screenKey)
            }

            else -> dialogsQueue.update {
                addLast(screenKey)
            }
        }
    }

    private fun forwardFullScreen(screen: Screen, fullScreen: ComposeScreen.Full) {
        closeAllDialogs()

        val screenKey = screen.key
        val fullScreens = fullScreensQueue.value

        if (!screens.containsKey(screenKey)) {
            screens[screenKey] = screen
            composeScreens[screenKey] = fullScreen
        }

        when {
            fullScreens.lastOrNull() == screenKey -> return
            fullScreens.contains(screenKey) -> fullScreensQueue.update {
                remove(screenKey)
                addLast(screenKey)
            }

            else -> fullScreensQueue.update {
                addLast(screenKey)
            }
        }
    }

    private fun closeAllDialogs() {
        if (dialogsQueue.value.isEmpty()) {
            return
        }

        dialogsQueue.update {
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

        fullScreensQueue.update {
            while (last() != screenKey) {
                val removedScreen = removeLast()
                clearScreenData(removedScreen)
            }
        }
    }

    private fun back() {
        val currentScreen = currentVisibleScreen ?: return
        val screenKey = currentScreen.key
        val backHandler = backHandlers[screenKey]

        if (backHandler == null || backHandler.canExitScreen()) {
            backHandlers.remove(screenKey)
            remove(currentScreen)
        }
    }

    private fun backToRoot() {
        if (fullScreensQueue.value.isEmpty() || fullScreensQueue.value.size == 1) {
            return
        }

        navigationDelegate.onBackingToRoot()
        fullScreensQueue.update {
            val firstScreen = removeFirst()

            removeAll { screenKey ->
                clearScreenData(screenKey)
                true
            }

            addLast(firstScreen)
        }
    }

    private fun clearScreenData(screenKey: String) {
        screens.remove(screenKey)
        composeScreens.remove(screenKey)
    }

    private fun releaseNavigator() {
        dialogsQueue.update(ArrayDeque<*>::clear)
        fullScreensQueue.update(ArrayDeque<*>::clear)
        screens.clear()
    }

    private fun exitNavigator() {
        releaseNavigator()
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
        val isInitial = screen.key == fullScreensQueue.value.firstOrNull()
        navigationDelegate.onRemovingScreen(screen, isInitial)
    }

    protected open fun notifyBackingToScreen(screen: Screen) {
        val isInitial = screen.key == fullScreensQueue.value.firstOrNull()
        navigationDelegate.onBackingToScreen(screen, isInitial)
    }

    private fun <R : Any> MutableState<ArrayDeque<String>>.update(
        onUpdate: ArrayDeque<String>.() -> R?
    ): R? {
        val newState = ArrayDeque(value)
        val returnType = onUpdate.invoke(newState)
        value = newState

        return returnType
    }
}
