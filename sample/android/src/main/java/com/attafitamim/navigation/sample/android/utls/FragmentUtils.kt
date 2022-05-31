package com.attafitamim.navigation.sample.android.utls

import android.os.Bundle
import androidx.fragment.app.Fragment
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun Fragment.argumentsString(key: String? = null) = object : ReadWriteProperty<Any, String> {

    override fun getValue(thisRef: Any, property: KProperty<*>): String
            = requireArguments().getString(key ?: property.name).let(::requireNotNull)

    override fun setValue(thisRef: Any, property: KProperty<*>, value: String)
            = provideArguments().putString(key ?: property.name, value)
}

fun Fragment.provideArguments(): Bundle = arguments ?: Bundle().also { newArguments ->
    arguments = newArguments
}