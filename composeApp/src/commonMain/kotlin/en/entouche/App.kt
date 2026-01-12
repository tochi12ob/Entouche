package en.entouche

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import en.entouche.ui.navigation.AppNavigation
import en.entouche.ui.theme.EntoucheTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    EntoucheTheme(
        darkTheme = true  // Default to dark theme for the Calm Ocean glassmorphism look
    ) {
        AppNavigation()
    }
}
