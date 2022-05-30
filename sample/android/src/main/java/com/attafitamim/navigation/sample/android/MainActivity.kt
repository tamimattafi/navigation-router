package com.attafitamim.navigation.sample.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.attafitamim.navigation.router.android.navigator.ActivityNavigator
import com.attafitamim.navigation.router.android.screens.AndroidScreen
import com.attafitamim.navigation.router.base.navigator.SimpleNavigationBuffer
import com.attafitamim.navigation.router.base.result.SimpleResultWire
import com.attafitamim.navigation.router.base.router.SimpleNavigationRouter
import com.attafitamim.navigation.router.core.screens.platform.ScreenAdapter

class MainActivity : AppCompatActivity(), ScreenAdapter<AndroidScreen> {

    // Initialize buffer, wire and router only one time during app lifetime
    private val navigationBuffer = SimpleNavigationBuffer(::runOnUiThread)
    private val resultWire = SimpleResultWire()
    // Inject the router in places where you want to use it
    private val router = SimpleNavigationRouter(navigationBuffer, resultWire)

    // One time per activity (Or fragment if it has sub-fragments and plays the role of the navigator)
    private val navigator = ActivityNavigator(this, R.id.fragmentContainer, this)


    override fun onResumeFragments() {
        super.onResumeFragments()

        // Navigation key is optional, null is also considered as a key
        navigationBuffer.setNavigator(navigator, NAVIGATOR_KEY)
    }

    override fun onPause() {
        // Navigation key is optional, null is also considered as a key
        navigationBuffer.removeNavigator(NAVIGATOR_KEY)

        super.onPause()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    private companion object {
        // Navigation key is optional, null is also considered as a key
        const val NAVIGATOR_KEY = "main_navigator"
    }
}