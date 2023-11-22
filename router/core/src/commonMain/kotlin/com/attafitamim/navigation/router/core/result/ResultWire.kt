package com.attafitamim.navigation.router.core.result

import com.attafitamim.navigation.router.core.global.Disposable

interface ResultWire {

    /**
     * Sets data listener with given key
     * and returns [Disposable] for availability to dispose subscription.
     *
     * After first call listener will be removed.
     */
    fun <T> setResultListener(key: ResultKey<T>, listener: ResultListener<T>): Disposable

    /**
     * Sends data to listener with given key.
     */
    fun <T> sendResult(key: ResultKey<T>, data: T)
}
