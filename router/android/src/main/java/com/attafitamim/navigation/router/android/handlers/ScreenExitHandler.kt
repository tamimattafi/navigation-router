package com.attafitamim.navigation.router.android.handlers

import androidx.fragment.app.FragmentTransaction
import com.attafitamim.navigation.router.core.screens.Screen

interface FragmentTransactionProcessor {
    fun onAttachingFragment(
        transaction: FragmentTransaction,
        screen: Screen,
        isInitial: Boolean
    ) {}

    fun onRemovingScreen(
        screenKey: String,
        isInitial: Boolean
    ) {}

    fun onBackingToScreen(
        screenKey: String,
        isInitial: Boolean
    ) {}

    fun onBackingToRoot() {}
}
