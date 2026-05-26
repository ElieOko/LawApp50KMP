package emy.partners.lawapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVLayerVideoGravityResizeAspect
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerLayer
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.Foundation.NSURL
import platform.UIKit.UIView

fun MainViewController() = ComposeUIViewController { App() }

//@Composable
//actual fun PlatformVideoPlayer(url: String) {
//    UIKitView(
//        factory = {
//            val player = AVPlayer(NSURL(string = url))
//            val playerLayer = AVPlayerLayer(player)
//            playerLayer
//        }
//    )
//}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PlatformVideoPlayer(
    url: String,
    modifier: Modifier,
    isPlaying: Boolean
) {
    val player = remember(url) {
        AVPlayer(NSURL(string = url))
    }

    val playerLayer = remember(player) {
        AVPlayerLayer(player)
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) player.play() else player.pause()
    }

    DisposableEffect(player) {
        onDispose {
            player.pause()
            player.replaceCurrentItemWithPlayerItem(null)
        }
    }

    UIKitView(
        modifier = modifier,
        factory = {
            val view = UIView()

            playerLayer.videoGravity = AVLayerVideoGravityResizeAspect
            view.layer.addSublayer(playerLayer)

            view
        },
        update = { view ->
            playerLayer.frame = view.bounds
        }
    )
}
