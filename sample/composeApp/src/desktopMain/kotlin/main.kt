import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.attafitamim.navigation.common.router.ApplicationRouter
import com.attafitamim.navigation.common.router.NavigationScreen
import com.attafitamim.navigation.router.compose.navigator.ComposeNavigator
import com.attafitamim.navigation.sample.compose.ComposeScreenAdapter

fun main() {
    val navigator = ComposeNavigator(ComposeScreenAdapter)
    ApplicationRouter.navigatorHolder.setNavigator(navigator)
    ApplicationRouter.instance.newRootScreen(NavigationScreen.Main)

    application {
        Window(onCloseRequest = ::exitApplication, title = "Router Sample") {
            navigator.Content()
        }
    }
}
