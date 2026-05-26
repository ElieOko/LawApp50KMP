package emy.partners.lawapp

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import emy.partners.lawapp.presentation.pages.HomePage

@Composable
@Preview
fun App() {
    MaterialTheme {
        Scaffold {
            HomePage()
        }
    }
}

@Composable
expect fun PlatformVideoPlayer(url: String, modifier: Modifier, isPlaying: Boolean)