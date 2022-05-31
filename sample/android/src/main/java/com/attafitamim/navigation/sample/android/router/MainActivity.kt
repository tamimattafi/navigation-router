package com.attafitamim.navigation.sample.android.router

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.attafitamim.navigation.common.router.NavigationScreen
import com.attafitamim.navigation.router.android.navigator.ActivityNavigator
import com.attafitamim.navigation.sample.android.R

class MainActivity : AppCompatActivity() {

    // One time per activity (Or fragment if it has sub-fragments and plays the role of the navigator)
    private val navigator = ActivityNavigator(this, R.id.fragmentContainer, ScreenAdapter)

    override fun onResumeFragments() {
        super.onResumeFragments()

        // Navigation key is optional, null is also considered as a key
        ApplicationRouter.navigatorHolder.setNavigator(navigator)
    }

    override fun onPause() {
        // Navigation key is optional, null is also considered as a key
        ApplicationRouter.navigatorHolder.removeNavigator()

        super.onPause()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ApplicationRouter.instance.newRootChain(NavigationScreen.Simple)
    }

    override fun onBackPressed() {
        ApplicationRouter.instance.exit()
    }
}