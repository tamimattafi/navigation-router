package com.attafitamim.navigation.router.core.result

fun interface ResultListener<T> {
    fun onResult(data: T)
}
