package com.attafitamim.navigation.router.android.navigator

import android.content.ActivityNotFoundException
import android.content.Intent
import android.view.KeyEvent
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.LifecycleOwner
import com.attafitamim.navigation.router.android.handlers.FragmentTransactionProcessor
import com.attafitamim.navigation.router.android.screens.AndroidScreen
import com.attafitamim.navigation.router.core.commands.Command
import com.attafitamim.navigation.router.core.handlers.ScreenBackPressHandler
import com.attafitamim.navigation.router.core.navigator.Navigator
import com.attafitamim.navigation.router.core.screens.Screen
import com.attafitamim.navigation.router.core.screens.platform.ScreenAdapter

abstract class BaseNavigator(
    protected val activity: FragmentActivity,
    protected open val containerId: Int,
    protected open val screenAdapter: ScreenAdapter<AndroidScreen>,
    protected open val fragmentManager: FragmentManager,
    protected open val fragmentFactory: FragmentFactory,
    protected open val lifecycleOwner: LifecycleOwner,
    protected open val fragmentTransactionProcessor: FragmentTransactionProcessor?,
    protected open val keepAfterLastFragment: Boolean
) : Navigator {

    protected open val screenHistory = mutableListOf<String>()
    protected open val initialFragment: Fragment? get() = fragmentManager.fragments.firstOrNull()
    protected open val initialScreenKey: String? get() = initialFragment?.tag

    protected open val currentVisibleScreenKey: String? get() = currentVisibleFragment?.tag
    protected open val currentVisibleFragment: Fragment? get() =
        fragmentManager.fragments.lastOrNull(Fragment::isVisible)

    protected open lateinit var backPressCallback: OnBackPressedCallback
    protected open val Screen.isVisible get() = key == fragmentManager.fragments.lastOrNull()?.tag

    init {
        setupBackPressListener()
    }

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

    protected open fun releaseNavigator() {
        backPressCallback.apply {
            isEnabled = false
            remove()
        }

        exitNavigator()
    }

    protected open fun setCurrentScreenBackPressHandler(handler: ScreenBackPressHandler) {
        val backPressCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!isEnabled) return

                if (handler.canExitScreen()) {
                    isEnabled = false
                    remove()
                    performBackPress()
                }
            }
        }

        val currentFragment = this.currentVisibleFragment
        val lifecycleOwner = currentFragment?.viewLifecycleOwner ?: this.lifecycleOwner

        activity.onBackPressedDispatcher.addCallback(
            lifecycleOwner,
            backPressCallback
        )
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
            is Command.AddBackPressHandler -> setCurrentScreenBackPressHandler(command.handler)
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
        commitNewFragmentScreen(screen, androidScreen)
    }

    protected open fun dismissOpenDialogs() {
        if (currentVisibleFragment !is DialogFragment) return removeTopDialog()

        val screensToRemove = screenHistory.mapNotNull { screenKey ->
            val fragment = fragmentManager.findFragmentByTag(screenKey)
            if (fragment !is DialogFragment) null
            else screenKey to fragment
        }

        screensToRemove.forEach { screenPair ->
            removeScreen(screenPair.first, screenPair.second)
        }
    }

    protected open fun removeTopDialog() {
        val currentVisibleScreenKey = currentVisibleScreenKey ?: return
        val visibleFragment = currentVisibleFragment
        if (visibleFragment is DialogFragment) {
            removeScreen(currentVisibleScreenKey)
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
        releaseNavigator()
    }

    protected open fun replaceFragment(screen: Screen, androidScreen: AndroidScreen.Fragment) {
        dismissOpenDialogs()

        if (!currentVisibleScreenKey.isNullOrBlank()) {
            notifyRemovingCurrentScreen()
            fragmentManager.popBackStack(currentVisibleScreenKey, POP_BACK_STACK_INCLUSIVE)
        }

        commitNewFragmentScreen(screen, androidScreen)
    }

    protected open fun replaceDialog(screen: Screen, androidScreen: AndroidScreen.Dialog) {
        removeTopDialog()
        openNewDialogScreen(screen, androidScreen)
    }

    protected open fun performBackPress() {
        applyCommand(Command.Back)
    }

    protected open fun back() {
        val visibleScreen = currentVisibleScreenKey

        when {
            visibleScreen == null ||
                    screenHistory.isEmpty() -> releaseNavigator()

            screenHistory.size > 1 ||
                    keepAfterLastFragment -> removeScreen(visibleScreen)

            else -> releaseNavigator()
        }
    }

    protected open fun resetScreen(screen: Screen) {
        removeScreen(screen.key)
        screenHistory.add(screen.key)
    }

    protected open fun removeScreen(screenKey: String) {
        val fragment = fragmentManager.findFragmentByTag(screenKey) ?: return
        removeScreen(screenKey, fragment)
    }

    protected open fun removeScreen(screenKey: String, fragment: Fragment) {
        notifyRemovingScreen(screenKey)

        when {
            fragment is DialogFragment -> {
                fragment.dismiss()
                screenHistory.remove(screenKey)
            }

            currentVisibleFragment?.tag == screenKey -> {
                fragmentManager.popBackStackImmediate(screenKey, POP_BACK_STACK_INCLUSIVE)
                screenHistory.remove(screenKey)
            }

            screenHistory.contains(screenKey) -> {
                backTo(screenKey)
                fragmentManager.popBackStackImmediate(screenKey, POP_BACK_STACK_INCLUSIVE)
                screenHistory.remove(screenKey)
            }
        }
    }

    protected open fun commitNewFragmentScreen(
        screen: Screen,
        fragmentScreen: AndroidScreen.Fragment
    ) {
        if (screen.isVisible) return
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

        transaction.addToBackStack(screen.key)

        transaction.commit()
    }

    protected open fun openNewDialogScreen(screen: Screen, dialogScreen: AndroidScreen.Dialog) {
        if (screen.isVisible) return
        resetScreen(screen)

        val dialog = dialogScreen.createDialog(fragmentFactory)
        dialog.showNow(fragmentManager, screen.key)

        dialog.dialog?.setOnKeyListener { _, keyCode, event ->
            val shouldHandle = keyCode == KeyEvent.KEYCODE_BACK
                    && event.action == KeyEvent.ACTION_UP
                    && dialog.isCancelable

            if (shouldHandle) performBackPress()

            return@setOnKeyListener shouldHandle
        }
    }

    /**
     * Performs [Command.BackTo] command transition
     */
    protected open fun backTo(screen: Screen?) {
        if (screen == null) return backToRoot()
        backTo(screen.key)
    }

    protected open fun backTo(screenKey: String) {
        if (screenHistory.contains(screenKey)) {
            notifyBackingToScreen(screenKey)
            fragmentManager.popBackStackImmediate(screenKey, 0)

            while (screenHistory.last() != screenKey) {
                screenHistory.removeLast()
            }
        } else {
            backToUnexisting(screenKey)
        }
    }

    protected open fun backToRoot() {
        fragmentTransactionProcessor?.onBackingToRoot()
        dismissOpenDialogs()
        screenHistory.clear()
        fragmentManager.popBackStackImmediate(null, POP_BACK_STACK_INCLUSIVE)
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

    protected open fun notifyRemovingCurrentScreen() {
        val currentScreenKey = currentVisibleScreenKey
        if (currentScreenKey != null) notifyRemovingScreen(currentScreenKey)
    }

    protected open fun notifyRemovingScreen(screenKey: String) {
        val isInitial = screenKey == initialScreenKey
        fragmentTransactionProcessor?.onRemovingScreen(screenKey, isInitial)
    }

    protected open fun notifyBackingToScreen(screenKey: String) {
        val isInitial = screenKey == initialScreenKey
        fragmentTransactionProcessor?.onBackingToScreen(screenKey, isInitial)
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
    protected open fun backToUnexisting(screenKey: String) {
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

    private fun setupBackPressListener() {
        backPressCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!isEnabled) return
                performBackPress()
            }
        }

        activity.onBackPressedDispatcher.addCallback(
            lifecycleOwner,
            backPressCallback
        )
    }
}
