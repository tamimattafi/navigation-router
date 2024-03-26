import androidx.compose.ui.uikit.ComposeUIViewControllerDelegate
import androidx.compose.ui.uikit.OnFocusBehavior
import androidx.compose.ui.window.ComposeUIViewController
import com.attafitamim.navigation.common.router.ApplicationRouter
import com.attafitamim.navigation.router.compose.navigator.ComposeNavigationDelegate
import com.attafitamim.navigation.router.compose.navigator.ComposeNavigator
import com.attafitamim.navigation.router.compose.navigator.PlatformNavigatorConfiguration
import com.attafitamim.navigation.router.compose.screens.Destination
import com.attafitamim.navigation.router.compose.utils.ActionsStore
import com.attafitamim.navigation.router.compose.utils.actionsStore
import com.attafitamim.navigation.router.core.navigator.NavigatorHolder
import com.attafitamim.navigation.router.core.screens.Screen
import com.attafitamim.navigation.router.core.screens.platform.ScreenAdapter
import com.attafitamim.navigation.sample.compose.ComposeApp
import com.attafitamim.navigation.sample.compose.ComposeScreenAdapter
import platform.UIKit.UIViewController


class MainViewController : ComposeNavigationDelegate,
    ComposeUIViewControllerDelegate {

    private val adapter: ScreenAdapter<Destination> get() = ComposeScreenAdapter
    private val navigatorHolder: NavigatorHolder get() = ApplicationRouter.navigatorHolder

    private val navigator by lazy {
        ComposeNavigator(adapter, this)
    }

    private val platformController by lazy {
        ComposeUIViewController(
            configure = {
                this.delegate = this@MainViewController
                onFocusBehavior = OnFocusBehavior.DoNothing
            }
        ) {
            ComposeApp(navigator)
        }
    }

    fun onBackPressed() {
        actionsStore.send(ActionsStore.Action.OnBackPress)
        ApplicationRouter.instance.exit()
    }

    override fun viewWillAppear(animated: Boolean) {
        super.viewWillAppear(animated)
        navigatorHolder.setNavigator(navigator)
    }

    override fun viewWillDisappear(animated: Boolean) {
        navigatorHolder.removeNavigator()
        super.viewWillDisappear(animated)
    }

    fun getPlatformController(): UIViewController = platformController

    override fun preformExit() {
        platformController.dismissViewControllerAnimated(
            flag = true,
            completion = {}
        )
    }

    override fun handleExternal(screen: Screen, external: Destination.External) {
        external.forward(PlatformNavigatorConfiguration(platformController))
    }
}
