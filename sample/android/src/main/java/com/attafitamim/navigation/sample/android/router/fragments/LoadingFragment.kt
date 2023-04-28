package com.attafitamim.navigation.sample.android.router.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.attafitamim.navigation.common.router.NavigationScreen
import com.attafitamim.navigation.common.router.Results
import com.attafitamim.navigation.sample.android.R
import com.attafitamim.navigation.sample.android.router.ApplicationRouter
import com.attafitamim.navigation.sample.android.utls.argumentsString
import java.util.*

class LoadingFragment : DialogFragment(R.layout.fragment_loading) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Can exit dialog only through button
        isCancelable = false
    }
}