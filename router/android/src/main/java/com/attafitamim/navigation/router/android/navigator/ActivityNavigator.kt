package com.attafitamim.navigation.router.android.navigator

import android.content.ActivityNotFoundException
import android.content.Intent
import android.view.KeyEvent
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.*
import com.attafitamim.navigation.router.android.screens.*
import com.attafitamim.navigation.router.core.commands.Command
import com.attafitamim.navigation.router.core.handlers.ScreenExitHandler
import com.attafitamim.navigation.router.core.screens.platform.ScreenAdapter
import com.attafitamim.navigation.router.core.navigator.Navigator
import com.attafitamim.navigation.router.core.screens.Screen
import java.util.LinkedHashMap

open class ActivityNavigator @JvmOverloads constructor(
    protected val activity: FragmentActivity,
    protected val containerId: Int,
    protected val screenAdapter: ScreenAdapter<AndroidScreen>,
    protected val fragmentManager: FragmentManager = activity.supportFragmentManager,
    protected val fragmentFactory: FragmentFactory = fragmentManager.fragmentFactory
) : Navigator {

    private var screenExitHandler: ScreenExitHandler? = null

    protected val screenHistory = LinkedHashMap<String, Screen>()
    protected val localStackCopy = mutableListOf<String>()

    private val currentVisibleFragment get() =
        fragmentManager.fragments.lastOrNull()?.takeIf(Fragment::isVisible)

    override val currentVisibleScreen: Screen? get() =
        screenHistory[currentVisibleFragment?.tag]

    override fun applyCommands(commands: Array<out Command>) {
        fragmentManager.executePendingTransactions()

        //copy stack before apply commands
        copyStackToLocal()

        for (command in commands) {
            try {
                applyCommand(command)
            } catch (e: RuntimeException) {
                errorOnApplyCommand(command, e)
            }
        }
    }

    override fun setScreenExitHandler(handler: ScreenExitHandler) {
        this.screenExitHandler = handler
    }

    override fun removeScreenExitHandler() {
        this.screenExitHandler = null
    }

    private fun copyStackToLocal() {
        localStackCopy.clear()
        for (i in 0 until fragmentManager.backStackEntryCount) {
            localStackCopy.add(fragmentManager.getBackStackEntryAt(i).name.orEmpty())
        }
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
            is AndroidScreen.Activity -> {
                checkAndStartActivity(screen, androidScreen)
            }
            is AndroidScreen.Fragment -> {
                commitNewFragmentScreen(screen, androidScreen, true)
            }
            is AndroidScreen.Dialog -> {
                openNewDialogScreen(screen, androidScreen)
            }
        }
    }

    protected open fun replace(screen: Screen) {
        when (val androidScreen = screenAdapter.createPlatformScreen(screen)) {
            is AndroidScreen.Activity -> {
                checkAndStartActivity(screen, androidScreen)
                activity.finish()
            }

            is AndroidScreen.Fragment -> {
                if (localStackCopy.isNotEmpty()) {
                    fragmentManager.popBackStack()
                    localStackCopy.removeAt(localStackCopy.lastIndex)
                    commitNewFragmentScreen(screen, androidScreen, true)
                } else {
                    commitNewFragmentScreen(screen, androidScreen, false)
                }
            }

            is AndroidScreen.Dialog -> {
                openNewDialogScreen(screen, androidScreen)
            }
        }
    }

    protected open fun back() {
        val visibleScreen = currentVisibleScreen
        val canBackPress = visibleScreen == null || screenExitHandler?.canExitScreen(visibleScreen)
                ?: true

        if (canBackPress) performBack()
    }

    protected open fun performBack() {
        val visibleScreenKey = currentVisibleScreen?.key
        if (visibleScreenKey != null) removeScreen(visibleScreenKey)
        else activityBack()
    }

    protected fun removeScreen(key: String) {
        val fragment = fragmentManager.findFragmentByTag(key) ?: return
        if (fragment is DialogFragment) {
            fragment.dismiss()
            return
        }

        if (localStackCopy.isNotEmpty()) {
            if (fragment.isVisible) fragmentManager.popBackStack()
            else fragmentManager.beginTransaction().remove(fragment).commitNow()
            return
        }

        screenHistory.remove(key)
    }

    protected open fun activityBack() {
        activity.finish()
    }

    protected open fun commitNewFragmentScreen(
        screen: Screen,
        fragmentScreen: AndroidScreen.Fragment,
        addToBackStack: Boolean
    ) {
        val fragment = fragmentScreen.createFragment(fragmentFactory)
        val transaction = fragmentManager.beginTransaction()
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
        if (addToBackStack) {
            transaction.addToBackStack(screen.key)
            localStackCopy.add(screen.key)
        }
        transaction.commit()

        screenHistory[screen.key] = screen
    }

    protected open fun openNewDialogScreen(screen: Screen, dialogScreen: AndroidScreen.Dialog) {
        val dialog = dialogScreen.createDialog(fragmentFactory)
        dialog.show(fragmentManager, screen.key)
        dialog.isCancelable = false
        screenHistory[screen.key] = screen
    }

    /**
     * Performs [Command.BackTo] command transition
     */
    protected open fun backTo(screen: Screen?) {
        val androidScreen = screen?.let(screenAdapter::createPlatformScreen)
            ?: return backToRoot()

        val tag = screen.key
        val index = localStackCopy.indexOfFirst { it == tag }
        if (index != -1) {
            val forRemove = localStackCopy.subList(index, localStackCopy.size)
            fragmentManager.popBackStack(forRemove.first().toString(), 0)
            forRemove.clear()
        } else {
            backToUnexisting(androidScreen)
        }
    }

    private fun backToRoot() {
        localStackCopy.clear()
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

    private fun checkAndStartActivity(screen: Screen, activityScreen: AndroidScreen.Activity) {
        // Check if we can start activity
        val activityIntent = activityScreen.createIntent(activity)
        try {
            activity.startActivity(activityIntent, activityScreen.startActivityOptions)
        } catch (e: ActivityNotFoundException) {
            unexistingActivity(screen, activityScreen, activityIntent)
        }
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
