import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.attafitamim.navigation.common.router.ApplicationRouter
import com.attafitamim.navigation.common.router.NavigationScreen
import com.attafitamim.navigation.common.router.Results
import kotlin.random.Random
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

private const val SNACKBAR_DURATION = 10000L
private const val MAX_SNACK_TEXT = 3

private fun back() {
    ApplicationRouter.instance.exit()
}

private fun toSimpleScreen() {
    toScreen(NavigationScreen.Simple)
}

private fun toArgumentsScreen() {
    val screen = NavigationScreen.WithArguments(
        Random.nextLong().toString(),
        Random.nextLong().toString()
    )

    toScreen(screen)
}

private fun toScreen(screen: NavigationScreen) {
    ApplicationRouter.instance.navigateTo(screen)
}

private fun snackBar(text: String) {
    val snack = NavigationScreen.SnackBar(text)
    ApplicationRouter.instance.navigateTo(snack)
}

private fun sendSimpleResult() {
    ApplicationRouter.instance.sendResult(
        Results.OnResultKey,
        Random.nextLong().toString()
    )
}

private fun shareText(text: String) {
    val screen = NavigationScreen.TextShareDialog(text)
    ApplicationRouter.instance.navigateTo(screen)
}

@Composable
fun MainScreen() {
    val greeting = remember { Greeting().greet() }
    Column(
        Modifier.fillMaxSize().background(Color(0xFFB0FFD0)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Compose: $greeting")
        Button(onClick = ::toSimpleScreen) {
            Text("Simple Screen")
        }

        Button(onClick = ::toArgumentsScreen) {
            Text("Arguments Screen")
        }

        Button(onClick = ::back) {
            Text("Back")
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun SimpleScreen() {
    Column(
        Modifier.fillMaxWidth().background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = ::toArgumentsScreen) {
            Text("Arguments Screen")
        }

        Button(onClick = { ApplicationRouter.instance.exit() }) {
            Text("Back")
        }
    }
}

@Composable
fun ArgumentsScreen(
    screen: NavigationScreen.WithArguments
) {
    var exitAttempts by remember { mutableIntStateOf(0) }

    LaunchedEffect(screen) {
        ApplicationRouter.instance.setCurrentScreenExitHandler {
            val canExit = exitAttempts == 2

            if (!canExit) {
                exitAttempts++
            }

            canExit
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFDDD1FF)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Screen: $screen")

        Button(onClick = ::toSimpleScreen) {
            Text("Simple Screen")
        }

        Button(onClick = {
            toScreen(screen)
            snackBar("Same screen with arg1: ${screen.arg1}")
        }) {
            Text("Same Arguments Screen")
        }

        Button(onClick = ::toArgumentsScreen) {
            Text("New Arguments Screen")
        }

        Button(onClick = {
            shareText(screen.arg1)
        }) {
            Text("Share arg1")
        }

        Button(onClick = ::back) {
            Text("Back")
        }

        Text("Exit attempts: $exitAttempts")
    }
}

@Composable
fun ResultScreen(
    screen: NavigationScreen.WithResult
) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Screen: $screen")

        Button(onClick = ::toSimpleScreen) {
            Text("Simple Screen")
        }

        Button(onClick = ::sendSimpleResult) {
            Text("Send Result")
        }

        Button(onClick = ::back) {
            Text("Back")
        }
    }
}

@Composable
fun SnackBarLayout(
    message: String,
    onDismiss: () -> Unit
) {
    val snackBarHost = remember { SnackbarHostState() }
    Box(modifier = Modifier.fillMaxSize()) {
        SimpleSnackBar(
            snackBarHost = snackBarHost,
            message = message
        )
    }
    LaunchedEffect(Unit) {
        snackBarHost.showSnackbar(message, duration = SnackbarDuration.Short)
        delay(SNACKBAR_DURATION)
        onDismiss()
    }
}

@Composable
fun BoxScope.SimpleSnackBar(
    snackBarHost: SnackbarHostState,
    message: String
) {
    SnackbarHost(
        modifier = Modifier.align(Alignment.BottomCenter)
            .padding(bottom = 40.dp, start = 20.dp, end = 20.dp).heightIn(max = 90.dp),
        hostState = snackBarHost
    ) {
        SimpleToast(message = message)
    }
}

@Composable
private fun SimpleToast(
    message: String
) {
    Snackbar {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp)
        ) {
            Text(
                text = message,
                modifier = Modifier.align(Alignment.CenterVertically).weight(1f)
                    .padding(end = 15.dp),
                maxLines = MAX_SNACK_TEXT,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
