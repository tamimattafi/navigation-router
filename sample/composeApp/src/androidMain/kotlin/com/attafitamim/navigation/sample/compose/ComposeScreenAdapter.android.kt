package com.attafitamim.navigation.sample.compose

import android.content.Intent
import com.attafitamim.navigation.common.router.NavigationScreen
import com.attafitamim.navigation.router.compose.screens.Destination

actual fun toPlatformScreen(screen: NavigationScreen.TextShareDialog): Destination =
    Destination.External { configuration ->
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, screen.text)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        configuration.activity.startActivity(shareIntent)
    }
