package com.attafitamim.navigation.router.core.navigator

interface NavigatorHolder {
    fun setNavigator(navigator: Navigator, navigatorKey: String? = null)
    fun removeNavigator(navigatorKey: String? = null)
}
