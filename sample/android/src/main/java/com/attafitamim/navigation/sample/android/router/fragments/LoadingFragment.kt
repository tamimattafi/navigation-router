package com.attafitamim.navigation.sample.android.router.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.attafitamim.navigation.sample.android.R

class LoadingFragment : DialogFragment(R.layout.fragment_loading) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Can exit dialog only through button
        isCancelable = false
    }
}