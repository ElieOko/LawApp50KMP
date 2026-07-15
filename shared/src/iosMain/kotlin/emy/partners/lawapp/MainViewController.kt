package emy.partners.lawapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitViewController
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.seekToTime
import platform.AVKit.AVPlayerViewController
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSURL
import platform.Foundation.NSOperationQueue
import platform.darwin.NSObjectProtocol

fun MainViewController() = ComposeUIViewController { App() }

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PlatformVideoPlayer(
    url: String,
    modifier: Modifier,
    isPlaying: Boolean,
    isLooping: Boolean,
    showControls: Boolean,
) {
    val playerItem = remember(url) {
        val nsUrl = NSURL.URLWithString(url) ?: NSURL.fileURLWithPath(url)
        AVPlayerItem(uRL = nsUrl)
    }
    val player = remember(playerItem) {
        AVPlayer(playerItem = playerItem)
    }
    val controller = remember(player) {
        AVPlayerViewController().apply {
            this.player = player
            showsPlaybackControls = showControls
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
                player.seekToTime(CMTimeMakeWithSeconds(0.0, 600))
                if (isPlaying) player.play()
            }
        }
        onDispose {
            observer?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
            player.pause()
            controller.player = null
        }
    }

    SideEffect {
        controller.showsPlaybackControls = showControls
        if (isPlaying) player.play() else player.pause()
    }

    UIKitViewController(
        factory = { controller },
        modifier = modifier,
        update = {
            it.player = player
            it.showsPlaybackControls = showControls
            it.videoGravity = AVLayerVideoGravityResizeAspectFill
        },
    )
}
