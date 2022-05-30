package com.attafitamim.navigation.router.base.result

import com.attafitamim.navigation.router.core.result.ResultKey
import com.attafitamim.navigation.router.core.result.ResultListener
import com.attafitamim.navigation.router.core.result.ResultListenerHandler
import com.attafitamim.navigation.router.core.result.ResultWire

class SimpleResultWire : ResultWire {

    private val listeners = mutableMapOf<ResultKey<*>, MutableList<ResultListener<*>>>()

    override fun <T> setResultListener(
        key: ResultKey<T>,
        listener: ResultListener<T>
    ): ResultListenerHandler {
        provideListenersList(key).add(listener)
        return ResultListenerHandler {
            listeners[key]?.remove(listener)
        }
    }

    override fun <T> sendResult(key: ResultKey<T>, data: T) {
        val listener = listeners.remove(key) as? MutableList<ResultListener<T>>
        listener?.forEach { resultListener ->
            resultListener.onResult(data)
        }
    }

    private fun provideListenersList(resultKey: ResultKey<*>): MutableList<ResultListener<*>> =
        listeners[resultKey] ?: mutableListOf<ResultListener<*>>().also { list ->
            listeners[resultKey] = list
        }
}
