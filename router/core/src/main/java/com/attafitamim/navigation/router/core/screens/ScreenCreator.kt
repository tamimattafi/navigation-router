package com.attafitamim.navigation.router.core.screens

fun interface ScreenCreator<A, R> {
    fun create(argument: A): R
}
