package com.attafitamim.navigation.router.android.navigator

import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import com.attafitamim.navigation.router.android.handlers.FragmentTransactionProcessor
import com.attafitamim.navigation.router.android.screens.AndroidScreen
import com.attafitamim.navigation.router.core.screens.Screen
import com.attafitamim.navigation.router.core.screens.platform.ScreenAdapter

open class ActivityNavigator @JvmOverloads constructor(
    activity: FragmentActivity,
    containerId: Int,
    screenAdapter: ScreenAdapter<AndroidScreen>,
    fragmentManager: FragmentManager = activity.supportFragmentManager,
    fragmentFactory: FragmentFactory = fragmentManager.fragmentFactory,
    lifecycleOwner: LifecycleOwner = activity,
    fragmentTransactionProcessor: FragmentTransactionProcessor? = null,
    keepAfterLastFragment: Boolean = false,
    private val performExit: () -> Unit = activity::finish
) : BaseNavigator(
    activity,
    containerId,
    screenAdapter,
    fragmentManager,
    fragmentFactory,
    lifecycleOwner,
    fragmentTransactionProcessor,
    keepAfterLastFragment
) {

    override fun exitNavigator() {
        performExit.invoke()
    }

    override fun replaceActivity(screen: Screen, androidScreen: AndroidScreen.Activity) {
        super.replaceActivity(screen, androidScreen)
        performExit.invoke()
    }
}
