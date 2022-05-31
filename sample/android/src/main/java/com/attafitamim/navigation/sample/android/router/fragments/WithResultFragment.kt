package com.attafitamim.navigation.sample.android.router.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        txtArgs?.text = arg1

        btnResult?.setOnClickListener {
            publishResult()
        }

        btnComplexResult?.setOnClickListener {
            publishComplexResult()
        }
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