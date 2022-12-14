package com.attafitamim.navigation.router.android.handlers

import androidx.fragment.app.FragmentTransaction
import com.attafitamim.navigation.router.core.screens.Screen

fun interface FragmentTransactionProcessor {
    fun processTransaction(
        transaction: FragmentTransaction,
        screen: Screen,
        isInitial: Boolean
    )
}
