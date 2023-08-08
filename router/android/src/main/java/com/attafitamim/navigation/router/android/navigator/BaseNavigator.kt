package com.attafitamim.navigation.router.android.navigator

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.IntentSender
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
import com.attafitamim.navigation.router.android.handlers.NavigationDelegate
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
    protected open val navigationDelegate: NavigationDelegate
) : Navigator {

    override val currentVisibleScreen: Screen? get() =
        screenHistory[currentVisibleFragment?.tag]

    protected open val currentVisibleFragment: Fragment? get() =
        fragmentManager.fragments.lastOrNull(Fragment::isVisible)

    protected open val initialScreen: Screen? get() =
        screenHistory[initialFragment?.tag]

    protected open val initialFragment: Fragment? get() =
        fragmentManager.fragments.firstOrNull()

    protected open val topScreen: Screen? get() =
        screenHistory[topFragment?.tag]

    protected open val topFragment: Fragment? get() =
        fragmentManager.fragments.lastOrNull()

    protected open val screenHistory = mutableMapOf<String, Screen>()
    protected open lateinit var backPressCallback: OnBackPressedCallback


    init {
        setupBackPressListener()
    }

    override fun applyCommands(commands: Array<out Command>) {
        fragmentManager.executePendingTransactions()

        for (command in commands) {
            tryApplyCommand(command)
        }
    }

    protected open fun tryApplyCommand(command: Command) {
        if (navigationDelegate.shouldApplyCommand(command)) {
            try {
                applyCommand(command)
            } catch (e: RuntimeException) {
                errorOnApplyCommand(command, e)
            }
        }
    }

    protected open fun exitNavigator() {
        navigationDelegate.performExit(activity)
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
            is Command.Remove -> remove(command.screen)
        }
    }

    protected open fun forward(screen: Screen) {
        when (val androidScreen = screenAdapter.createPlatformScreen(screen)) {
            is AndroidScreen.Activity -> forwardActivity(screen, androidScreen)
            is AndroidScreen.Fragment -> forwardFragment(screen, androidScreen)
            is AndroidScreen.Dialog -> forwardDialog(screen, androidScreen)
            is AndroidScreen.Sender -> forwardSender(screen, androidScreen)
        }
    }

    protected open fun forwardActivity(screen: Screen, androidScreen: AndroidScreen.Activity) {
        checkAndStartActivity(screen, androidScreen)
    }

    protected open fun forwardSender(screen: Screen, androidScreen: AndroidScreen.Sender) {
        checkAndSendIntent(screen, androidScreen)
    }

    protected open fun forwardFragment(screen: Screen, androidScreen: AndroidScreen.Fragment) {
        dismissOpenDialogs()
        commitNewFragmentScreen(screen, androidScreen)
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
            is AndroidScreen.Sender -> replaceSender(screen, androidScreen)
        }
    }

    protected open fun replaceActivity(screen: Screen, androidScreen: AndroidScreen.Activity) {
        checkAndStartActivity(screen, androidScreen)
        releaseNavigator()
    }

    protected open fun replaceSender(screen: Screen, androidScreen: AndroidScreen.Sender) {
        checkAndSendIntent(screen, androidScreen)
        releaseNavigator()
    }

    protected open fun replaceFragment(screen: Screen, androidScreen: AndroidScreen.Fragment) {
        dismissOpenDialogs()

        val currentScreen = currentVisibleScreen
        if (currentScreen != null) removeScreen(currentScreen)

        commitNewFragmentScreen(screen, androidScreen)
    }

    protected open fun replaceDialog(screen: Screen, androidScreen: AndroidScreen.Dialog) {
        removeTopDialog()
        openNewDialogScreen(screen, androidScreen)
    }

    protected open fun performBackPress() {
        tryApplyCommand(Command.Back)
    }

    protected open fun remove(screen: Screen) {
        if (!screenHistory.containsValue(screen)) return

        when {
            screenHistory.size > 1 || navigationDelegate.keepAfterLastScreen(screen) -> {
                removeScreen(screen)
            }

            else -> releaseNavigator()
        }
    }

    protected open fun back() {
        val visibleScreen = currentVisibleScreen

        when {
            visibleScreen == null || screenHistory.isEmpty() -> {
                releaseNavigator()
            }

            screenHistory.size > 1 || navigationDelegate.keepAfterLastScreen(visibleScreen) -> {
                removeScreen(visibleScreen)
            }

            else -> releaseNavigator()
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
                fragmentManager.popBackStack(screen.key, POP_BACK_STACK_INCLUSIVE)
                screenHistory.remove(screen.key)
            }

            screenHistory.containsKey(screen.key) -> {
                backTo(screen)
                fragmentManager.popBackStack(screen.key, POP_BACK_STACK_INCLUSIVE)
                screenHistory.remove(screen.key)
            }
        }
    }

    protected open fun commitNewFragmentScreen(
        screen: Screen,
        fragmentScreen: AndroidScreen.Fragment
    ) {
        val topFragment = when (screen) {
            currentVisibleScreen -> return
            topScreen -> topFragment
            else -> null
        }

        val fragment = if (topFragment == null) {
            resetScreen(screen)
            fragmentScreen.createFragment(fragmentFactory)
        } else {
            topFragment
        }

        val transaction = fragmentManager.beginTransaction()
        navigationDelegate.onAttachingFragment(
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
        when (screen) {
            currentVisibleScreen -> return
            topScreen -> return
        }

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

        if (screenHistory.contains(screen.key)) {
            notifyBackingToScreen(screen)
            fragmentManager.popBackStack(screen.key, 0)

            while (screenHistory.values.lastOrNull() != screen) {
                screenHistory.remove(screenHistory.values.last().key)
            }
        } else {
            backToUnexisting(screen)
        }
    }

    protected open fun backToRoot() {
        navigationDelegate.onBackingToRoot()
        dismissOpenDialogs()
        screenHistory.clear()
        fragmentManager.popBackStack(null, POP_BACK_STACK_INCLUSIVE)
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

    protected open fun checkAndSendIntent(screen: Screen, senderScreen: AndroidScreen.Sender) {
        val intentSender = senderScreen.createIntentSender(activity)
        try {
            intentSender.sendIntent(
                activity,
                senderScreen.code,
                senderScreen.fillIntent,
                senderScreen.onFinish,
                senderScreen.handler
            )
        } catch (e: ActivityNotFoundException) {
            unexistingIntentSender(screen, senderScreen, intentSender)
        }
    }

    protected open fun notifyRemovingScreen(screen: Screen) {
        val isInitial = screen == initialScreen
        navigationDelegate.onRemovingScreen(screen, isInitial)
    }

    protected open fun notifyBackingToScreen(screen: Screen) {
        val isInitial = screen == initialScreen
        navigationDelegate.onBackingToScreen(screen, isInitial)
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
     * Called when there is no intent to open `tag`.
     *
     * @param screen         screen
     * @param intentSender intent sender used to start intent for the `tag`
     */
    protected open fun unexistingIntentSender(
        screen: Screen,
        senderScreen: AndroidScreen.Sender,
        intentSender: IntentSender
    ) {
        // Do nothing by default
    }

    /**
     * Called when we tried to fragmentBack to some specific screen (via [Command.BackTo] command),
     * but didn't found it.
     *
     * @param screen screen
     */
    protected open fun backToUnexisting(screen: Screen) {
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
