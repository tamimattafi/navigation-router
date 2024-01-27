import androidx.compose.material.MaterialTheme
import androidx.compose.ui.window.ComposeUIViewController
import com.attafitamim.navigation.common.router.ApplicationRouter
import com.attafitamim.navigation.common.router.NavigationScreen
import com.attafitamim.navigation.router.compose.handlers.ComposeNavigationDelegate
import com.attafitamim.navigation.router.compose.navigator.ComposeNavigator
import com.attafitamim.navigation.sample.compose.ComposeScreenAdapter
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    val navigator = ComposeNavigator(ComposeScreenAdapter, object : ComposeNavigationDelegate {
        override fun preformExit() {
            error("Exiting using crash for now :D")
        }
    })


    ApplicationRouter.navigatorHolder.setNavigator(navigator)
    ApplicationRouter.instance.newRootScreen(NavigationScreen.Main)

    return ComposeUIViewController {
        MaterialTheme {
            navigator.Content()
        }
    }
}
