package com.attafitamim.navigation.router.compose.screens

import platform.UIKit.UIViewController

actual fun interface ComposeNavigatorController : ComposeNavigatorScreen  {

    fun startController(rootViewController: UIViewController)
}
