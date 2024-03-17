package com.attafitamim.navigation.sample.compose

import android.content.Intent
import androidx.core.content.ContextCompat.startActivity
import com.attafitamim.navigation.common.router.NavigationScreen
import com.attafitamim.navigation.router.compose.screens.ComposeNavigatorController
import com.attafitamim.navigation.router.compose.screens.ComposeNavigatorScreen

actual fun toPlatformScreen(screen: NavigationScreen.TextShareDialog): ComposeNavigatorScreen =
    ComposeNavigatorController { activity ->
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, screen.text)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        activity.startActivity(shareIntent)
    }
