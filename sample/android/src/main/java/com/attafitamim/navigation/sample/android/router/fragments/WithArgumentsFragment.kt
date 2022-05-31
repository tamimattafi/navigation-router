package com.attafitamim.navigation.sample.android.router.fragments

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.attafitamim.navigation.sample.android.R
import com.attafitamim.navigation.sample.android.utls.argumentsString

class WithArgumentsFragment : Fragment(R.layout.fragment_with_arguments) {

    var arg1: String by argumentsString()
    var arg2: String by argumentsString()

    private val txtArgs get() = view?.findViewById<TextView>(R.id.txtArgs)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val argumentsString = buildString {
            append(arg1, "\n", arg2)
        }

        txtArgs?.text = argumentsString
    }
}
