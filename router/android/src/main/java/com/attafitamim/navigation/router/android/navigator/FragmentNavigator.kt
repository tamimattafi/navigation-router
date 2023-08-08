package com.attafitamim.navigation.router.android.navigator

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import com.attafitamim.navigation.router.android.handlers.ActivityNavigationDelegate
import com.attafitamim.navigation.router.android.handlers.FragmentNavigationDelegate
import com.attafitamim.navigation.router.android.handlers.NavigationDelegate
import com.attafitamim.navigation.router.android.screens.AndroidScreen
import com.attafitamim.navigation.router.core.screens.platform.ScreenAdapter

open class FragmentNavigator @JvmOverloads constructor(
    fragment: Fragment,
    containerId: Int,
    screenAdapter: ScreenAdapter<AndroidScreen>,
    fragmentManager: FragmentManager = fragment.childFragmentManager,
    fragmentFactory: FragmentFactory = fragmentManager.fragmentFactory,
    lifecycleOwner: LifecycleOwner = fragment,
    navigationDelegate: NavigationDelegate = FragmentNavigationDelegate.defaultInstance()
) : BaseNavigator(
    fragment.requireActivity(),
    containerId,
    screenAdapter,
    fragmentManager,
    fragmentFactory,
    lifecycleOwner,
    navigationDelegate,
)
