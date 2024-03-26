package com.attafitamim.navigation.sample.compose

import com.attafitamim.navigation.common.router.NavigationScreen
import com.attafitamim.navigation.router.compose.screens.Destination
import platform.UIKit.UIActivityViewController
import platform.UIKit.popoverPresentationController

actual fun toPlatformScreen(screen: NavigationScreen.TextShareDialog): Destination =
    Destination.External { configuration ->
        val textToShare = listOf(screen.text)
        val activityViewController = UIActivityViewController(activityItems = textToShare, applicationActivities = null)
        activityViewController.popoverPresentationController?.sourceView = configuration.uiViewController.view
        configuration.uiViewController.presentViewController(activityViewController, animated = true, completion = null)
    }
