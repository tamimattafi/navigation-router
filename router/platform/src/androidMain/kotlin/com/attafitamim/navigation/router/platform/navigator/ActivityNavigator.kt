package com.attafitamim.navigation.router.platform.navigator

import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import com.attafitamim.navigation.router.platform.handlers.ActivityNavigationDelegate
import com.attafitamim.navigation.router.platform.handlers.NavigationDelegate
import com.attafitamim.navigation.router.platform.screens.AndroidScreen
import com.attafitamim.navigation.router.core.screens.platform.ScreenAdapter

open class ActivityNavigator @JvmOverloads constructor(
    activity: FragmentActivity,
    containerId: Int,
    screenAdapter: ScreenAdapter<AndroidScreen>,
    fragmentManager: FragmentManager = activity.supportFragmentManager,
    fragmentFactory: FragmentFactory = fragmentManager.fragmentFactory,
    lifecycleOwner: LifecycleOwner = activity,
    navigationDelegate: NavigationDelegate = ActivityNavigationDelegate.defaultInstance()
) : BaseNavigator(
    activity,
    containerId,
    screenAdapter,
    fragmentManager,
    fragmentFactory,
    lifecycleOwner,
    navigationDelegate
)