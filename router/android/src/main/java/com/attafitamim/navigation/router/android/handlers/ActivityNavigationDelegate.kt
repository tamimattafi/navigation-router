package com.attafitamim.navigation.router.android.handlers

import android.app.Activity

interface ActivityNavigationDelegate : NavigationDelegate {

    override fun performExit(activity: Activity) = activity.finish()

    companion object {
        fun defaultInstance() = object : ActivityNavigationDelegate {}
    }
}

