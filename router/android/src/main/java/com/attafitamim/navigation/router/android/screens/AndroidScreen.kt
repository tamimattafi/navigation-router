package com.attafitamim.navigation.router.android.screens

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.attafitamim.navigation.router.core.screens.Screen
import com.attafitamim.navigation.router.core.screens.ScreenCreator

sealed interface AndroidScreen : Screen

interface ActivityScreen : AndroidScreen {
    val startActivityOptions: Bundle? get() = null
    fun createIntent(context: Context): Intent

    companion object {
        operator fun invoke(
            key: String? = null,
            startActivityOptions: Bundle? = null,
            intentCreator: ScreenCreator<Context, Intent>
        ) = object : ActivityScreen {
            override val key = key ?: intentCreator::class.java.name
            override val startActivityOptions = startActivityOptions
            override fun createIntent(context: Context) = intentCreator.create(context)
        }
    }
}

interface FragmentScreen : AndroidScreen {
    val clearContainer: Boolean get() = true
    fun createFragment(factory: FragmentFactory): Fragment

    companion object {
        operator fun invoke(
            key: String? = null,
            clearContainer: Boolean = true,
            fragmentCreator: ScreenCreator<FragmentFactory, Fragment>
        ) = object : FragmentScreen {
            override val key = key ?: fragmentCreator::class.java.name
            override val clearContainer = clearContainer
            override fun createFragment(factory: FragmentFactory) = fragmentCreator.create(factory)
        }
    }
}

interface DialogScreen : AndroidScreen {
    val clearContainer: Boolean get() = true
    fun createDialog(factory: FragmentFactory): DialogFragment

    companion object {
        operator fun invoke(
            key: String? = null,
            clearContainer: Boolean = true,
            fragmentCreator: ScreenCreator<FragmentFactory, DialogFragment>
        ) = object : DialogScreen {
            override val key = key ?: fragmentCreator::class.java.name
            override val clearContainer = clearContainer
            override fun createDialog(factory: FragmentFactory) = fragmentCreator.create(factory)
        }
    }
}
