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
    override val activity: FragmentActivity,
    override val containerId: Int,
    override val screenAdapter: ScreenAdapter<AndroidScreen>,
    override val fragmentManager: FragmentManager = activity.supportFragmentManager,
    override val fragmentFactory: FragmentFactory = fragmentManager.fragmentFactory,
    override val lifecycleOwner: LifecycleOwner = activity,
    override val fragmentTransactionProcessor: FragmentTransactionProcessor? = null
) : BaseNavigator() {

    override fun exitNavigator() {
        activity.finish()
    }

    override fun replaceActivity(screen: Screen, androidScreen: AndroidScreen.Activity) {
        super.replaceActivity(screen, androidScreen)
        activity.finish()
    }
}
