package com.attafitamim.navigation.router.base.router

import com.attafitamim.navigation.router.base.navigator.SimpleNavigationBuffer
import com.attafitamim.navigation.router.core.commands.Command
import com.attafitamim.navigation.router.core.navigator.NavigationBuffer
import com.attafitamim.navigation.router.core.result.ResultKey
import com.attafitamim.navigation.router.core.result.ResultListener
import com.attafitamim.navigation.router.core.result.ResultListenerHandler
import com.attafitamim.navigation.router.core.result.ResultWire
import com.attafitamim.navigation.router.core.router.Router

/**
 * BaseRouter is an abstract class to implement high-level navigation.
 *
 * Extend it to add needed transition methods.
 */
abstract class BaseRouter(
    internal val commandBuffer: NavigationBuffer,
    private val resultWire: ResultWire
) : Router {

    override fun <T> setResultListener(
        key: ResultKey<T>,
        listener: ResultListener<T>
    ): ResultListenerHandler {
        return resultWire.setResultListener(key, listener)
    }

    override fun <T> sendResult(key: ResultKey<T>, data: T) {
        resultWire.sendResult(key, data)
    }

    /**
     * Sends navigation command array to [SimpleNavigationBuffer].
     *
     * @param commands navigation command array to execute
     */
    protected fun executeCommands(navigatorKey: String? = null, vararg commands: Command) {
        commandBuffer.applyCommands(navigatorKey, commands)
    }
}
