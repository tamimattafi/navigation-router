package com.attafitamim.navigation.router.base.result

import com.attafitamim.navigation.router.core.commands.MessageHandler
import com.attafitamim.navigation.router.core.global.Disposable
import com.attafitamim.navigation.router.core.result.ResultKey
import com.attafitamim.navigation.router.core.result.ResultListener
import com.attafitamim.navigation.router.core.result.ResultWire

class SimpleResultWire(
    private val messageHandler: MessageHandler
) : ResultWire {

    private val listeners = HashMap<ResultKey<*>, ResultListener<*>>()

    override fun <T> setResultListener(
        key: ResultKey<T>,
        listener: ResultListener<T>
    ): Disposable {
        addListener(key, listener)
        return Disposable {
            removeKey(key)
        }
    }

    override fun <T> sendResult(key: ResultKey<T>, data: T) {
        messageHandler.post {
            val listener = listeners.remove(key) as? ResultListener<T> ?: return@post
            listener.onResult(data)
        }
    }

    private fun removeKey(key: ResultKey<*>) = messageHandler.post {
        listeners.remove(key)
    }

    private fun addListener(
        key: ResultKey<*>,
        listener: ResultListener<*>
    ) = messageHandler.post {
        listeners[key] = listener
    }
}
