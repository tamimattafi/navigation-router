package com.attafitamim.navigation.router.android.navigator

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import com.attafitamim.navigation.router.android.handlers.FragmentTransactionProcessor
import com.attafitamim.navigation.router.android.screens.AndroidScreen
import com.attafitamim.navigation.router.core.screens.platform.ScreenAdapter

open class FragmentNavigator @JvmOverloads constructor(
    private val fragment: Fragment,
    override val containerId: Int,
    override val screenAdapter: ScreenAdapter<AndroidScreen>,
    override val fragmentManager: FragmentManager = fragment.childFragmentManager,
    override val fragmentFactory: FragmentFactory = fragmentManager.fragmentFactory,
    override val lifecycleOwner: LifecycleOwner = fragment,
    override val fragmentTransactionProcessor: FragmentTransactionProcessor? = null
) : BaseNavigator() {

    override val activity: FragmentActivity
        get() = fragment.requireActivity()

    override fun exitNavigator() {
        activity.onBackPressed()
    }
}
