package com.attafitamim.navigation.router.platform.screens

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment as AndroidFragment
import androidx.fragment.app.FragmentFactory
import com.attafitamim.navigation.router.core.screens.factory.ScreenCreator
import com.attafitamim.navigation.router.core.screens.platform.PlatformScreen

sealed interface AndroidScreen : PlatformScreen {

    interface Activity : AndroidScreen {
        val startActivityOptions: Bundle? get() = null
        fun createIntent(context: Context): Intent

        companion object {
            operator fun invoke(
                startActivityOptions: Bundle? = null,
                intentCreator: ScreenCreator<Context, Intent>
            ) = object : Activity {
                override val startActivityOptions = startActivityOptions
                override fun createIntent(context: Context) = intentCreator.create(context)
            }
        }
    }

    interface Sender : AndroidScreen {
        val code: Int get() = 0
        val onFinish: IntentSender.OnFinished? get() = null
        val handler: Handler? get() = null
        val fillIntent: Intent? get() = null
        fun createIntentSender(context: Context): IntentSender
        companion object {
            operator fun invoke(
                code: Int = 0,
                fillIntent: Intent?,
                onFinish: IntentSender.OnFinished? = null,
                handler: Handler? = null,
                intentSenderCreator: ScreenCreator<Context, IntentSender>
            ) = object : Sender {
                override val code: Int = code
                override val fillIntent: Intent? = fillIntent
                override val onFinish: IntentSender.OnFinished? = onFinish
                override val handler: Handler? = handler

                override fun createIntentSender(context: Context): IntentSender =
                    intentSenderCreator.create(context)
            }
        }
    }

    interface Fragment : AndroidScreen {
        val clearContainer: Boolean get() = true
        fun createFragment(factory: FragmentFactory): AndroidFragment

        companion object {
            operator fun invoke(
                clearContainer: Boolean = true,
                fragmentCreator: ScreenCreator<FragmentFactory, AndroidFragment>
            ) = object : Fragment {
                override val clearContainer = clearContainer
                override fun createFragment(factory: FragmentFactory) = fragmentCreator.create(factory)
            }
        }
    }

    interface Dialog : AndroidScreen {
        val clearContainer: Boolean get() = true
        fun createDialog(factory: FragmentFactory): DialogFragment

        companion object {
            operator fun invoke(
                clearContainer: Boolean = true,
                fragmentCreator: ScreenCreator<FragmentFactory, DialogFragment>
            ) = object : Dialog {
                override val clearContainer = clearContainer
                override fun createDialog(factory: FragmentFactory) = fragmentCreator.create(factory)
            }
        }
    }
}
