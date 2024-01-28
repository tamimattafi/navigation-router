import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import com.attafitamim.navigation.common.router.ApplicationRouter
import com.attafitamim.navigation.common.router.NavigationScreen
import com.attafitamim.navigation.router.compose.navigator.ComposeNavigationDelegate
import com.attafitamim.navigation.router.compose.navigator.ComposeNavigator
import com.attafitamim.navigation.router.compose.utils.ActionsStore
import com.attafitamim.navigation.router.compose.utils.actionsStore
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
        Box(Modifier.safeDrawingPadding()) {
            MaterialTheme {
                navigator.Content()
            }
        }
    }
}

fun onBackPress() {
    actionsStore.send(ActionsStore.Action.OnBackPress)
}
