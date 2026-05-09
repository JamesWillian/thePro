package app.jammes.thepro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.jammes.thepro.presentation.navigation.AppNavGraph
import app.jammes.thepro.presentation.ui.theme.ThePROTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ThePROTheme {
                AppNavGraph()
            }
        }
    }
}
