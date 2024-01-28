package com.attafitamim.navigation.router.compose.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

val actionsStore by lazy {
    CoroutineScope(SupervisorJob()).createStore()
}

interface ActionsStore {
    fun send(action: Action)
    val events: SharedFlow<Action>

    sealed interface Action {
        data object OnBackPress : Action
    }
}

fun CoroutineScope.createStore(): ActionsStore {
    val events = MutableSharedFlow<ActionsStore.Action>()

    return object : ActionsStore {
        override fun send(action: ActionsStore.Action) {
            launch {
                events.emit(action)
            }
        }
        override val events: SharedFlow<ActionsStore.Action> = events.asSharedFlow()
    }
}

