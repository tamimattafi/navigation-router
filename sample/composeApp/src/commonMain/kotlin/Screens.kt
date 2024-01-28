import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.attafitamim.navigation.common.router.ApplicationRouter
import com.attafitamim.navigation.common.router.NavigationScreen
import com.attafitamim.navigation.common.router.Results
import kotlin.random.Random
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource


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

private fun sendSimpleResult() {
    ApplicationRouter.instance.sendResult(
        Results.OnResultKey,
        Random.nextLong().toString()
    )
}

@Composable
fun MainScreen() {
    val greeting = remember { Greeting().greet() }
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
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
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Image(painterResource("compose-multiplatform.xml"), null)
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
        modifier = Modifier.fillMaxSize().background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Screen: $screen")

        Button(onClick = ::toSimpleScreen) {
            Text("Simple Screen")
        }

        Button(onClick = { toScreen(screen) }) {
            Text("Same Arguments Screen")
        }

        Button(onClick = ::toArgumentsScreen) {
            Text("New Arguments Screen")
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