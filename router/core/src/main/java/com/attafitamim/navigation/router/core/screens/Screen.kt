package com.attafitamim.navigation.router.core.screens

interface Screen {
    val key: String get() = buildString {
        append(javaClass.name, hashCode())
    }
}
