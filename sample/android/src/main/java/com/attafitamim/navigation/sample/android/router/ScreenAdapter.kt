package com.attafitamim.navigation.sample.android.router

import android.content.Intent
import android.net.Uri
import android.os.Build
import com.attafitamim.navigation.common.router.NavigationScreen
import com.attafitamim.navigation.router.android.screens.AndroidScreen
import com.attafitamim.navigation.router.core.screens.Screen
import com.attafitamim.navigation.router.core.screens.platform.ScreenAdapter
import com.attafitamim.navigation.sample.android.router.fragments.LoadingFragment
import com.attafitamim.navigation.sample.android.router.fragments.MainFragment
import com.attafitamim.navigation.sample.android.router.fragments.SimpleFragment
import com.attafitamim.navigation.sample.android.router.fragments.WithArgumentsFragment
import com.attafitamim.navigation.sample.android.router.fragments.WithResultFragment

object ScreenAdapter : ScreenAdapter<AndroidScreen> {

    override fun createPlatformScreen(screen: Screen): AndroidScreen =
        when(val navigationScreen = screen as NavigationScreen) {
            is NavigationScreen.Main -> AndroidScreen.Fragment {
                MainFragment()
            }

            is NavigationScreen.Simple -> AndroidScreen.Fragment {
                SimpleFragment()
            }

            is NavigationScreen.Loading -> AndroidScreen.Dialog {
                LoadingFragment()
            }

            is NavigationScreen.WithArguments -> AndroidScreen.Fragment {
                WithArgumentsFragment().apply {
                    arg1 = navigationScreen.arg1
                    arg2 = navigationScreen.arg2
                }
            }

            is NavigationScreen.WithComplexResult -> AndroidScreen.Dialog {
                WithResultFragment().apply {
                    arg1 = navigationScreen.arg1
                }
            }

            is NavigationScreen.WithResult -> AndroidScreen.Dialog {
                WithResultFragment().apply {
                    arg1 = navigationScreen.arg1
                }
            }

            is NavigationScreen.PlayMarket -> {
                val packageName = "com.android.vending"

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    AndroidScreen.Sender(
                        code = 0,
                        fillIntent = null,
                        onFinish = null,
                        handler = null
                    ) { context ->
                        context.packageManager.getLaunchIntentSenderForPackage(packageName)
                    }
                } else {
                    AndroidScreen.Activity { context ->
                        val fallbackUri = Uri.parse("https://play.google.com/store")
                        val fallBackIntent = Intent(Intent.ACTION_VIEW, fallbackUri)

                        context.packageManager.getLaunchIntentForPackage(packageName)
                            ?: fallBackIntent
                    }
                }
            }
        }
}

