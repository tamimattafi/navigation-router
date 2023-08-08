package com.attafitamim.navigation.router.android.handlers

import android.app.Activity

interface FragmentNavigationDelegate : NavigationDelegate {

    override fun performExit(activity: Activity) = activity.onBackPressed()

    companion object {
        fun defaultInstance() = object : FragmentNavigationDelegate {}
    }
}
