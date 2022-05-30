package com.attafitamim.navigation.router.android.navigator

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.fragment.app.*
import com.attafitamim.navigation.router.android.screens.*
import com.attafitamim.navigation.router.core.commands.Command
import com.attafitamim.navigation.router.core.screens.ScreenAdapter
import com.attafitamim.navigation.router.core.navigator.Navigator
import com.attafitamim.navigation.router.core.screens.Screen

open class AndroidAppNavigator @JvmOverloads constructor(
    protected val activity: FragmentActivity,
    protected val containerId: Int,
    protected val screenAdapter: ScreenAdapter<AndroidScreen>,
    protected val fragmentManager: FragmentManager = activity.supportFragmentManager,
    protected val fragmentFactory: FragmentFactory = fragmentManager.fragmentFactory
) : Navigator {

    protected val localStackCopy = mutableListOf<String>()

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
            is Command.Forward -> forward(screenAdapter.createPlatformScreen(command.screen))
            is Command.Replace -> replace(screenAdapter.createPlatformScreen(command.screen))
            is Command.BackTo -> backTo(command.screen?.let(screenAdapter::createPlatformScreen))
            is Command.Back -> back()
        }
    }

    protected open fun forward(screen: AndroidScreen) {
        when (screen) {
            is ActivityScreen -> {
                checkAndStartActivity(screen)
            }
            is FragmentScreen -> {
                commitNewFragmentScreen(screen, true)
            }
            is DialogScreen -> {
                openNewDialogScreen(screen)
            }
        }
    }

    protected open fun replace(screen: AndroidScreen) {
        when (screen) {
            is ActivityScreen -> {
                checkAndStartActivity(screen)
                activity.finish()
            }
            is FragmentScreen -> {
                if (localStackCopy.isNotEmpty()) {
                    fragmentManager.popBackStack()
                    localStackCopy.removeAt(localStackCopy.lastIndex)
                    commitNewFragmentScreen(screen, true)
                } else {
                    commitNewFragmentScreen(screen, false)
                }
            }
            is DialogScreen -> {
                openNewDialogScreen(screen)
            }
        }
    }

    protected open fun back() {
        if (localStackCopy.isNotEmpty()) {
            fragmentManager.popBackStack()
            localStackCopy.removeAt(localStackCopy.lastIndex)
        } else {
            activityBack()
        }
    }

    protected open fun activityBack() {
        activity.finish()
    }

    protected open fun commitNewFragmentScreen(
        screen: FragmentScreen,
        addToBackStack: Boolean
    ) {
        val fragment = screen.createFragment(fragmentFactory)
        val transaction = fragmentManager.beginTransaction()
        transaction.setReorderingAllowed(true)
        setupFragmentTransaction(
            screen,
            transaction,
            fragmentManager.findFragmentById(containerId),
            fragment
        )
        if (screen.clearContainer) {
            transaction.replace(containerId, fragment, screen.key)
        } else {
            transaction.add(containerId, fragment, screen.key)
        }
        if (addToBackStack) {
            transaction.addToBackStack(screen.key)
            localStackCopy.add(screen.key)
        }
        transaction.commit()
    }

    protected open fun openNewDialogScreen(screen: DialogScreen) {
        val dialog = screen.createDialog(fragmentFactory)
        dialog.show(fragmentManager, screen.key)
    }

    /**
     * Performs [Command.BackTo] command transition
     */
    protected open fun backTo(screen: AndroidScreen?) {
        if (screen == null) return backToRoot()
        val tag = screen.key
        val index = localStackCopy.indexOfFirst { it == tag }
        if (index != -1) {
            val forRemove = localStackCopy.subList(index, localStackCopy.size)
            fragmentManager.popBackStack(forRemove.first().toString(), 0)
            forRemove.clear()
        } else {
            backToUnexisting(screen)
        }
    }

    private fun backToRoot() {
        localStackCopy.clear()
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
        screen: FragmentScreen,
        fragmentTransaction: FragmentTransaction,
        currentFragment: Fragment?,
        nextFragment: Fragment
    ) {
        // Do nothing by default
    }

    private fun checkAndStartActivity(screen: ActivityScreen) {
        // Check if we can start activity
        val activityIntent = screen.createIntent(activity)
        try {
            activity.startActivity(activityIntent, screen.startActivityOptions)
        } catch (e: ActivityNotFoundException) {
            unexistingActivity(screen, activityIntent)
        }
    }

    /**
     * Called when there is no activity to open `tag`.
     *
     * @param screen         screen
     * @param activityIntent intent passed to start Activity for the `tag`
     */
    protected open fun unexistingActivity(
        screen: ActivityScreen,
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
