package com.attafitamim.navigation.router.android.navigator

import android.content.ActivityNotFoundException
import android.content.Intent
import android.view.KeyEvent
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.LifecycleOwner
import com.attafitamim.navigation.router.android.handlers.FragmentTransactionProcessor
import com.attafitamim.navigation.router.android.screens.AndroidScreen
import com.attafitamim.navigation.router.core.commands.Command
import com.attafitamim.navigation.router.core.handlers.ScreenExitHandler
import com.attafitamim.navigation.router.core.navigator.Navigator
import com.attafitamim.navigation.router.core.screens.Screen
import com.attafitamim.navigation.router.core.screens.platform.ScreenAdapter

abstract class BaseNavigator : Navigator {

    override val currentVisibleScreen: Screen? get() =
        screenHistory[currentVisibleFragment?.tag]

    protected open val currentVisibleFragment: Fragment? get() =
        fragmentManager.fragments.lastOrNull(Fragment::isVisible)

    protected open val initialFragment: Fragment? get() =
        fragmentManager.fragments.firstOrNull()

    protected open val initialScreen: Screen? get() =
        screenHistory[initialFragment?.tag]

    protected open val screenHistory = mutableMapOf<String, Screen>()

    protected abstract val activity: FragmentActivity
    protected abstract val containerId: Int
    protected abstract val screenAdapter: ScreenAdapter<AndroidScreen>
    protected abstract val fragmentManager: FragmentManager
    protected abstract val fragmentFactory: FragmentFactory
    protected abstract val lifecycleOwner: LifecycleOwner
    protected abstract val fragmentTransactionProcessor: FragmentTransactionProcessor?
    protected abstract val keepAfterLastFragment: Boolean
    protected open var screenExitHandler: ScreenExitHandler? = null


    protected abstract fun exitNavigator()

    override fun applyCommands(commands: Array<out Command>) {
        fragmentManager.executePendingTransactions()

        for (command in commands) {
            try {
                applyCommand(command)
            } catch (e: RuntimeException) {
                errorOnApplyCommand(command, e)
            }
        }
    }

    override fun setScreenExitCallbackHandler(handler: ScreenExitHandler) {
        this.screenExitHandler = handler
    }

    override fun removeScreenExitCallbackHandler() {
        this.screenExitHandler = null
    }

    /**
     * Perform transition described by the navigation command
     *
     * @param command the navigation command to apply
     */
    protected open fun applyCommand(command: Command) {
        when (command) {
            is Command.Forward -> forward(command.screen)
            is Command.Replace -> replace(command.screen)
            is Command.BackTo -> backTo(command.screen)
            is Command.Back -> back()
        }
    }

    protected open fun forward(screen: Screen) {
        when (val androidScreen = screenAdapter.createPlatformScreen(screen)) {
            is AndroidScreen.Activity -> forwardActivity(screen, androidScreen)
            is AndroidScreen.Fragment -> forwardFragment(screen, androidScreen)
            is AndroidScreen.Dialog -> forwardDialog(screen, androidScreen)
        }
    }

    protected open fun forwardActivity(screen: Screen, androidScreen: AndroidScreen.Activity) {
        checkAndStartActivity(screen, androidScreen)
    }

    protected open fun forwardFragment(screen: Screen, androidScreen: AndroidScreen.Fragment) {
        dismissOpenDialogs()
        commitNewFragmentScreen(screen, androidScreen, true)
    }

    protected open fun dismissOpenDialogs() {
        if (currentVisibleFragment !is DialogFragment) return removeTopDialog()

        val screensToRemove = screenHistory.values.mapNotNull { screen ->
            val fragment = fragmentManager.findFragmentByTag(screen.key)
            if (fragment !is DialogFragment) null
            else screen to fragment
        }

        screensToRemove.forEach { screenPair ->
            removeScreen(screenPair.first, screenPair.second)
        }
    }

    protected open fun removeTopDialog() {
        val currentVisibleScreen = currentVisibleScreen ?: return
        val visibleFragment = currentVisibleFragment
        if (visibleFragment is DialogFragment) {
            removeScreen(currentVisibleScreen)
        }
    }

    protected open fun forwardDialog(screen: Screen, androidScreen: AndroidScreen.Dialog) {
        openNewDialogScreen(screen, androidScreen)
    }

    protected open fun replace(screen: Screen) {
        when (val androidScreen = screenAdapter.createPlatformScreen(screen)) {
            is AndroidScreen.Activity -> replaceActivity(screen, androidScreen)
            is AndroidScreen.Fragment -> replaceFragment(screen, androidScreen)
            is AndroidScreen.Dialog -> replaceDialog(screen, androidScreen)
        }
    }

    protected open fun replaceActivity(screen: Screen, androidScreen: AndroidScreen.Activity) {
        checkAndStartActivity(screen, androidScreen)
        exitNavigator()
    }

    protected open fun replaceFragment(screen: Screen, androidScreen: AndroidScreen.Fragment) {
        dismissOpenDialogs()
        if (screenHistory.isNotEmpty()) {
            notifiyRemovingCurrentScreen()
            fragmentManager.popBackStack()
            commitNewFragmentScreen(screen, androidScreen, true)
        } else {
            commitNewFragmentScreen(screen, androidScreen, false)
        }
    }

    protected open fun replaceDialog(screen: Screen, androidScreen: AndroidScreen.Dialog) {
        removeTopDialog()
        openNewDialogScreen(screen, androidScreen)
    }

    protected open fun back() {
        val visibleScreen = currentVisibleScreen
        val canBackPress = visibleScreen == null || screenExitHandler?.canExitScreen(visibleScreen)
                ?: true

        if (canBackPress) performBack()
    }

    protected open fun performBack() {
        val visibleScreen = currentVisibleScreen

        when {
            visibleScreen == null ||
            screenHistory.isEmpty() -> exitNavigator()

            screenHistory.size > 1 ||
            keepAfterLastFragment -> removeScreen(visibleScreen)

            else -> exitNavigator()
        }
    }

    protected open fun resetScreen(screen: Screen) {
        removeScreen(screen)
        screenHistory[screen.key] = screen
    }

    protected open fun removeScreen(screen: Screen) {
        val fragment = fragmentManager.findFragmentByTag(screen.key) ?: return
        removeScreen(screen, fragment)
    }

    protected open fun removeScreen(screen: Screen, fragment: Fragment) {
        notifyRemovingScreen(screen)

        when {
            fragment is DialogFragment -> {
                fragment.dismiss()
                screenHistory.remove(screen.key)
            }

            currentVisibleFragment?.tag == screen.key -> {
                fragmentManager.popBackStack()
                screenHistory.remove(screen.key)
            }

            screenHistory.containsKey(screen.key) -> {
                backTo(screen)
                fragmentManager.popBackStack()
                screenHistory.remove(screen.key)
            }
        }
    }

    protected open fun commitNewFragmentScreen(
        screen: Screen,
        fragmentScreen: AndroidScreen.Fragment,
        addToBackStack: Boolean
    ) {
        resetScreen(screen)
        val fragment = fragmentScreen.createFragment(fragmentFactory)
        val transaction = fragmentManager.beginTransaction()
        fragmentTransactionProcessor?.onAttachingFragment(
            transaction,
            screen,
            fragmentManager.fragments.isEmpty()
        )

        transaction.setReorderingAllowed(true)
        setupFragmentTransaction(
            fragmentScreen,
            transaction,
            fragmentManager.findFragmentById(containerId),
            fragment
        )
        if (fragmentScreen.clearContainer) {
            transaction.replace(containerId, fragment, screen.key)
        } else {
            transaction.add(containerId, fragment, screen.key)
        }

        if (addToBackStack) transaction.addToBackStack(screen.key)

        transaction.commit()
    }

    protected open fun openNewDialogScreen(screen: Screen, dialogScreen: AndroidScreen.Dialog) {
        resetScreen(screen)
        val dialog = dialogScreen.createDialog(fragmentFactory)
        dialog.showNow(fragmentManager, screen.key)

        dialog.dialog?.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                back()
                true
            } else false
        }
    }

    /**
     * Performs [Command.BackTo] command transition
     */
    protected open fun backTo(screen: Screen?) {
        val androidScreen = screen?.let(screenAdapter::createPlatformScreen)
            ?: return backToRoot()

        if (screenHistory.containsKey(screen.key)) {
            notifyBackingToScreen(screen)
            fragmentManager.popBackStack(screen.key, 0)

            while (screenHistory.values.lastOrNull() != screen) {
                screenHistory.remove(screenHistory.values.last().key)
            }
        } else {
            backToUnexisting(androidScreen)
        }
    }

    protected open fun backToRoot() {
        fragmentTransactionProcessor?.onBackingToRoot()
        dismissOpenDialogs()
        screenHistory.clear()
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    /**
     * Override this method to setup fragment transaction [FragmentTransaction].
     * For example: setCustomAnimations(...), addSharedElement(...) or setReorderingAllowed(...)
     *
     * @param fragmentTransaction fragment transaction
     * @param currentFragment     current fragment in container
     *                            (for [Replace] command it will be screen previous in new chain, NOT replaced screen)
     * @param nextFragment        next screen fragment
     */
    protected open fun setupFragmentTransaction(
        screen: AndroidScreen.Fragment,
        fragmentTransaction: FragmentTransaction,
        currentFragment: Fragment?,
        nextFragment: Fragment
    ) {
        // Do nothing by default
    }

    protected open fun checkAndStartActivity(screen: Screen, activityScreen: AndroidScreen.Activity) {
        // Check if we can start activity
        val activityIntent = activityScreen.createIntent(activity)
        try {
            activity.startActivity(activityIntent, activityScreen.startActivityOptions)
        } catch (e: ActivityNotFoundException) {
            unexistingActivity(screen, activityScreen, activityIntent)
        }
    }

    protected open fun notifiyRemovingCurrentScreen() {
        val currentScreen = currentVisibleScreen
        if (currentScreen != null) notifyRemovingScreen(currentScreen)
    }

    protected open fun notifyRemovingScreen(screen: Screen) {
        val isInitial = screen == initialScreen
        fragmentTransactionProcessor?.onRemovingScreen(screen, isInitial)
    }

    protected open fun notifyBackingToScreen(screen: Screen) {
        val isInitial = screen == initialScreen
        fragmentTransactionProcessor?.onBackingToScreen(screen, isInitial)
    }

    /**
     * Called when there is no activity to open `tag`.
     *
     * @param screen         screen
     * @param activityIntent intent passed to start Activity for the `tag`
     */
    protected open fun unexistingActivity(
        screen: Screen,
        activityScreen: AndroidScreen.Activity,
        activityIntent: Intent
    ) {
        // Do nothing by default
    }

    /**
     * Called when we tried to fragmentBack to some specific screen (via [Command.BackTo] command),
     * but didn't found it.
     *
     * @param screen screen
     */
    protected open fun backToUnexisting(screen: AndroidScreen) {
        backToRoot()
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
}
