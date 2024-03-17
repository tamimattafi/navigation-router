package com.attafitamim.navigation.sample.compose

import com.attafitamim.navigation.common.router.NavigationScreen
import com.attafitamim.navigation.router.compose.screens.ComposeNavigatorController
import com.attafitamim.navigation.router.compose.screens.ComposeNavigatorScreen
import platform.UIKit.UIActivityViewController
import platform.UIKit.popoverPresentationController

actual fun toPlatformScreen(screen: NavigationScreen.TextShareDialog): ComposeNavigatorScreen =
    ComposeNavigatorController { rootViewController ->
        val textToShare = listOf(screen.text)
        val activityViewController = UIActivityViewController(activityItems = textToShare, applicationActivities = null)
        activityViewController.popoverPresentationController?.sourceView = rootViewController.view
        rootViewController.presentViewController(activityViewController, animated = true, completion = null)
    }
