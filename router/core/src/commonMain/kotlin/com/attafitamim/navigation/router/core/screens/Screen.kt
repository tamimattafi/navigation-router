package com.attafitamim.navigation.router.core.screens

interface Screen {
    val key: String get() {
        val name = this::class.simpleName.orEmpty()

        @OptIn(ExperimentalStdlibApi::class)
        val hashCode = this.hashCode().toHexString()
        return "$name#$hashCode"
    }
}
