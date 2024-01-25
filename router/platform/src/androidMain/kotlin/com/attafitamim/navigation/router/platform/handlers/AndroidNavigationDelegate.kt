package com.attafitamim.navigation.router.platform.handlers

import android.app.Activity
import androidx.fragment.app.FragmentTransaction
import com.attafitamim.navigation.router.core.commands.Command
import com.attafitamim.navigation.router.core.navigator.NavigationDelegate
import com.attafitamim.navigation.router.core.screens.Screen

interface AndroidNavigationDelegate : NavigationDelegate {

    fun performExit(activity: Activity)

    fun onAttachingFragment(
        transaction: FragmentTransaction,
        screen: Screen,
        isInitial: Boolean
    ) {}
}

