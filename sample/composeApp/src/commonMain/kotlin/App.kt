import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.attafitamim.navigation.common.router.ApplicationRouter
import com.attafitamim.navigation.common.router.NavigationScreen
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

@Composable
fun MainScreen() {
    MaterialTheme {
        val greeting = remember { Greeting().greet() }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Compose: $greeting")
            Button(onClick = { ApplicationRouter.instance.navigateTo(NavigationScreen.Simple) }) {
                Text("Simple Screen")
            }

            Button(onClick = { ApplicationRouter.instance.exit() }) {
                Text("Back")
            }
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