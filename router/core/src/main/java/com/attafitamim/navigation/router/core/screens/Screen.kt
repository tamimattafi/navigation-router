package com.attafitamim.navigation.router.core.screens

interface Screen {
    val key: String get() = this::class.java.name
}
