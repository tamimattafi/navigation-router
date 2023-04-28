package com.attafitamim.navigation.router.core.router

import com.attafitamim.navigation.router.core.global.Disposable
import com.attafitamim.navigation.router.core.handlers.ScreenBackPressHandler
import com.attafitamim.navigation.router.core.screens.Screen

interface NavigationRouter : Router {

    /**
     * Open new screen and add it to the screens chain.
     *
     * @param screen screen to be opened
     * @param navigatorKey navigator that should be used to handle the screen
     *
     */
    fun navigateTo(screen: Screen, navigatorKey: String? = null)

    /**
     * Clear all screens and open new one as root.
     *
     * @param screen screen to be opened
     * @param navigatorKey navigator that should be used to handle the screen
     *
     */
    fun newRootScreen(screen: Screen, navigatorKey: String? = null)

    /**
     * Replace current screen.
     *
     * By replacing the screen, you alters the backstack,
     * so by going fragmentBack you will return to the previous screen
     * and not to the replaced one.
     *
     * @param screen screen to be opened
     * @param navigatorKey navigator that should be used to handle the screen
     *
     */
    fun replaceScreen(screen: Screen, navigatorKey: String? = null)

    /**
     * Removes screen from the foreground or the backstack.
     *
     * @param screen screen to be removed
     * @param navigatorKey navigator that should be used to handle the screen
     *
     */
    fun removeScreen(screen: Screen, navigatorKey: String? = null)

    /**
     * Return fragmentBack to the needed screen from the chain.
     *
     * Behavior in the case when no needed screens found depends on
     * the processing of the [BackTo] command in a [Navigator] implementation.
     *
     * @param screen screen to be opened
     * @param navigatorKey navigator that should be used to handle the screen
     *
     */
    fun backTo(screen: Screen?, navigatorKey: String? = null)

    /**
     * Opens several screens inside single transaction.
     *
     * @param screens screens to be opened
     * @param navigatorKey navigator that should be used to handle the screens
     *
     */
    fun newChain(vararg screens: Screen, navigatorKey: String? = null)

    /**
     * Clear current stack and open several screens inside single transaction.
     *
     * @param screens screens to be opened
     * @param navigatorKey navigator that should be used to handle the screens
     *
     */
    fun newRootChain(vararg screens: Screen, navigatorKey: String? = null)

    /**
     * Remove all screens from the chain and exit.
     *
     * It's mostly used to finish the application or close a supplementary navigation chain.
     *
     * @param navigatorKey navigator that should be used to handle the operation
     *
     */
    fun finishChain(navigatorKey: String? = null)

    /**
     * Return to the previous screen in the chain.
     *
     * Behavior in the case when the current screen is the root depends on
     * the processing of the [Back] command in a [Navigator] implementation.
     *
     * @param navigatorKey navigator that should be used to handle the operation
     *
     */
    fun exit(navigatorKey: String? = null)

    /**
     * Handle back press for fragments and dialogs
     *
     * @param navigatorKey navigator that should be used to handle the operation
     * @param handler exit callback handler
     *
     */
    fun setCurrentScreenExitHandler(
        navigatorKey: String? = null,
        handler: ScreenBackPressHandler
    )
}
