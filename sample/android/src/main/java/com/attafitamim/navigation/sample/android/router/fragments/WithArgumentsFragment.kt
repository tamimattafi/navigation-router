package com.attafitamim.navigation.sample.android.router.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.attafitamim.navigation.common.router.NavigationScreen
import com.attafitamim.navigation.sample.android.R
import com.attafitamim.navigation.sample.android.router.ApplicationRouter
import com.attafitamim.navigation.sample.android.utls.argumentsString
import java.util.*

class WithArgumentsFragment : Fragment(R.layout.fragment_with_arguments) {

    var arg1: String by argumentsString()
    var arg2: String by argumentsString()

    private val txtArgs get() = view?.findViewById<TextView>(R.id.txtArgs)
    private val btnReattach get() = view?.findViewById<Button>(R.id.btnReattach)
    private val btnReattachDifferentArgs get() = view?.findViewById<Button>(R.id.btnReattachDifferentArgs)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val argumentsString = buildString {
            append(arg1, "\n", arg2)
        }

        txtArgs?.text = argumentsString

        btnReattach?.setOnClickListener {
            val screen = NavigationScreen.WithArguments(arg1, arg2)
            ApplicationRouter.instance.navigateTo(screen)
        }

        btnReattachDifferentArgs?.setOnClickListener {
            openArgumentsScreen()
        }
    }

    private fun openArgumentsScreen() {
        val screen = NavigationScreen.WithArguments(
            arg1 = "Some arguments from ${javaClass.simpleName}",
            arg2 = "Random int ${Random().nextInt()}"
        )

        ApplicationRouter.instance.navigateTo(screen)
    }
}
