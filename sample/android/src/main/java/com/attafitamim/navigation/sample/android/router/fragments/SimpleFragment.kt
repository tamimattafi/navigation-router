package com.attafitamim.navigation.sample.android.router.fragments

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.attafitamim.navigation.common.router.NavigationScreen
import com.attafitamim.navigation.common.router.Results
import com.attafitamim.navigation.sample.android.R
import com.attafitamim.navigation.sample.android.router.ApplicationRouter
import java.util.*

class SimpleFragment : Fragment(R.layout.fragment_simple) {

    private val txtResult get() = view?.findViewById<TextView>(R.id.txtResult)
    private var resultText: String? = null
    set(value) {
        field = value
        txtResult?.text = value
    }

    private val btnWithResult get() = view?.findViewById<Button>(R.id.btnResultScreen)
    private val btnWithArguments get() = view?.findViewById<Button>(R.id.btnArgumentsScreen)
    private val btnPlayStore get() = view?.findViewById<Button>(R.id.btnPlayStore)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnWithArguments?.setOnClickListener {
            openArgumentsScreen()
        }

        btnWithResult?.setOnClickListener {
            openResultScreen()
        }

        btnPlayStore?.setOnClickListener {
            openPlayStoreScreen()
        }

        txtResult?.text = resultText

        var clickCount = 0
        val maxClickCount = 2
        ApplicationRouter.instance.setCurrentScreenExitHandler(MainFragment.NAVIGATOR_KEY) {
            clickCount += 1
            Toast.makeText(
                requireContext(),
                "Press back button ${maxClickCount - clickCount} times to exit",
                Toast.LENGTH_SHORT
            ).show()

            clickCount == maxClickCount
        }
    }

    private fun openPlayStoreScreen() {
        ApplicationRouter.instance.navigateTo(NavigationScreen.PlayMarket)
    }

    private fun openArgumentsScreen() {
        val screen = NavigationScreen.WithArguments(
            arg1 = "Some arguments from ${javaClass.simpleName}",
            arg2 = "Random int ${Random().nextInt()}"
        )

        ApplicationRouter.instance.navigateTo(screen)
    }

    private fun openResultScreen() {
        val argument = """
                    Some arguments from ${javaClass.simpleName}
                    Random int: ${Random().nextInt()}
                """.trimIndent()

        val screen = NavigationScreen.WithResult(argument)

        ApplicationRouter.instance.apply {
            setResultListener(Results.OnResultKey) { result ->
                resultText = result
            }

            setResultListener(Results.OnComplexResultKey) { complexResult ->
                resultText = complexResult.toString()
            }

            navigateTo(screen)
        }
    }
}
