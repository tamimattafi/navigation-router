package com.attafitamim.navigation.sample.android.router.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.attafitamim.navigation.common.router.NavigationScreen
import com.attafitamim.navigation.common.router.Results
import com.attafitamim.navigation.sample.android.R
import com.attafitamim.navigation.sample.android.router.ApplicationRouter
import com.attafitamim.navigation.sample.android.utls.argumentsString
import java.util.*

class WithResultFragment : DialogFragment(R.layout.fragment_with_result) {

    var arg1: String by argumentsString()

    private val txtArgs get() = view?.findViewById<TextView>(R.id.txtArgs)
    private val btnResult get() = view?.findViewById<Button>(R.id.btnResult)
    private val btnComplexResult get() = view?.findViewById<Button>(R.id.btnComplexResult)
    private val btnOpenTopFragment get() = view?.findViewById<Button>(R.id.btnOpenTopFragment)
    private val btnOpenTopDialog get() = view?.findViewById<Button>(R.id.btnOpenTopDialog)
    private val btnReplaceDialog get() = view?.findViewById<Button>(R.id.btnReplaceDialog)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        txtArgs?.text = arg1

        btnResult?.setOnClickListener {
            publishResult()
        }

        btnComplexResult?.setOnClickListener {
            publishComplexResult()
        }

        btnOpenTopFragment?.setOnClickListener {
            openFragmentOnTop()
        }

        btnOpenTopDialog?.setOnClickListener {
            openDialogOnTop()
        }

        btnReplaceDialog?.setOnClickListener {
            replaceDialog()
        }
    }

    private fun replaceDialog() {
        val arguments = """
            Replaced ${javaClass.simpleName}
            Random int: ${Random().nextInt()}
        """.trimIndent()
        ApplicationRouter.instance.replaceScreen(NavigationScreen.WithResult(arguments))
    }

    private fun openDialogOnTop() {
        val arguments = """
            Opened from ${javaClass.simpleName}
            Random int: ${Random().nextInt()}
        """.trimIndent()
        ApplicationRouter.instance.navigateTo(NavigationScreen.WithResult(arguments))
    }

    private fun openFragmentOnTop() {
        val arguments = "Fragment opened from dialog"
        ApplicationRouter.instance.navigateTo(NavigationScreen.WithArguments(arguments, arguments))
    }

    private fun publishResult() {
        val result = """
                Some result from ${javaClass.simpleName}
                Random int: ${Random().nextInt()}
            """.trimIndent()

        ApplicationRouter.instance.apply {
            exit()
            sendResult(Results.OnResultKey, result)
        }
    }

    private fun publishComplexResult() {
        val complexResult = Results.ComplexResult(
            p1 = "Some argument from ${javaClass.simpleName}",
            p2 = "Random int: ${Random().nextInt()}"
        )

        ApplicationRouter.instance.apply {
            exit()
            sendResult(Results.OnComplexResultKey, complexResult)
        }
    }
}