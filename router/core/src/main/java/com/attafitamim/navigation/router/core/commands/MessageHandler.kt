package com.attafitamim.navigation.router.core.commands

interface MessageHandler {
    fun post(runnable: Runnable): Boolean
}
