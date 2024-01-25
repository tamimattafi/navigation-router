package com.attafitamim.navigation.router.platform.handlers

import android.app.Activity

interface FragmentNavigationDelegate : AndroidNavigationDelegate {

    @Suppress("DEPRECATION")
    override fun performExit(activity: Activity) = activity.onBackPressed()

    companion object {
        fun defaultInstance() = object : FragmentNavigationDelegate {}
    }
}
