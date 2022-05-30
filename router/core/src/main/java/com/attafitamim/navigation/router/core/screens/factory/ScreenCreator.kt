package com.attafitamim.navigation.router.core.screens.factory

fun interface ScreenCreator<A, R> {
    fun create(argument: A): R
}
