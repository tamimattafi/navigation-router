package com.attafitamim.navigation.router.android.handlers

import android.app.Activity
import androidx.fragment.app.FragmentTransaction
import com.attafitamim.navigation.router.core.commands.Command
import com.attafitamim.navigation.router.core.screens.Screen

interface NavigationDelegate {

    fun performExit(activity: Activity)

    fun shouldApplyCommand(command: Command): Boolean = true

    fun onAttachingFragment(
        transaction: FragmentTransaction,
        screen: Screen,
        isInitial: Boolean
    ) {}

    fun onRemovingScreen(
        screen: Screen,
        isInitial: Boolean
    ) {}

    fun onBackingToScreen(
        screen: Screen,
        isInitial: Boolean
    ) {}

    fun onBackingToRoot() {}

    fun keepAfterLastScreen(lastScreen: Screen): Boolean = false
}
