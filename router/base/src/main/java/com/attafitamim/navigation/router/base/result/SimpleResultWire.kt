package com.attafitamim.navigation.router.base.result

import com.attafitamim.navigation.router.core.commands.MessageHandler
import com.attafitamim.navigation.router.core.result.ResultKey
import com.attafitamim.navigation.router.core.result.ResultListener
import com.attafitamim.navigation.router.core.global.Disposable
import com.attafitamim.navigation.router.core.result.ResultWire
import java.util.concurrent.ConcurrentHashMap

class SimpleResultWire(
    private val messageHandler: MessageHandler
) : ResultWire {

    private val listeners = ConcurrentHashMap<ResultKey<*>, ResultListener<*>>()

    override fun <T> setResultListener(
        key: ResultKey<T>,
        listener: ResultListener<T>
    ): Disposable {
        listeners[key] = listener
        return Disposable {
            listeners.remove(key)
        }
    }

    override fun <T> sendResult(key: ResultKey<T>, data: T) {
        val listener = listeners.remove(key) as? ResultListener<T> ?: return
        messageHandler.post {
            listener.onResult(data)
        }
    }
}
