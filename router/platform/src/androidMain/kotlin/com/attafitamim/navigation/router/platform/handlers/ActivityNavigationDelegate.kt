package com.attafitamim.navigation.router.platform.handlers

import android.app.Activity

interface ActivityNavigationDelegate : AndroidNavigationDelegate {

    override fun performExit(activity: Activity) = activity.finish()

    companion object {
        fun defaultInstance() = object : ActivityNavigationDelegate {}
    }
}

