package com.attafitamim.navigation.router.core.screens

interface Screen {
    val key: String get() = buildString {
        append(this@Screen.javaClass.name, this@Screen.hashCode())
    }
}
