package emy.partners.lawapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
import platform.AVFoundation.AVPlayerLayer
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.seekToTime
import platform.CoreMedia.CMTimeMake
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSURL
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIView
import platform.darwin.NSObjectProtocol

fun MainViewController() = ComposeUIViewController { App() }

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PlatformVideoPlayer(
    url: String,
    modifier: Modifier,
    isPlaying: Boolean,
    isLooping: Boolean,
) {
    val playerItem = remember(url) {
        val nsUrl = NSURL.URLWithString(url) ?: NSURL.fileURLWithPath(url)
        AVPlayerItem(uRL = nsUrl)
    }
    val player = remember(playerItem) {
        AVPlayer(playerItem = playerItem)
    }
    val playerLayer = remember(player) {
        AVPlayerLayer.playerLayerWithPlayer(player).apply {
            videoGravity = AVLayerVideoGravityResizeAspectFill
        }
    }

    DisposableEffect(playerItem, isLooping) {
        var observer: NSObjectProtocol? = null
        if (isLooping) {
            observer = NSNotificationCenter.defaultCenter.addObserverForName(
                name = AVPlayerItemDidPlayToEndTimeNotification,
                `object` = playerItem,
                queue = NSOperationQueue.mainQueue,
            ) { _ ->
                player.seekToTime(CMTimeMake(0, 1))
                player.play()
            }
        }
        onDispose {
            observer?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
            player.pause()
            playerLayer.player = null
        }
    }

    SideEffect {
        if (isPlaying) player.play() else player.pause()
    }

    UIKitView(
        modifier = modifier,
        factory = {
            val view = UIView()
            view.layer.addSublayer(playerLayer)
            view
        },
        update = { view ->
            playerLayer.frame = view.bounds
        },
    )
}
