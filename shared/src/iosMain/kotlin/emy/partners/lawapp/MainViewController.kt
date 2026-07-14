package emy.partners.lawapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.UIKitView
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
import platform.AVFoundation.AVPlayerLayer
import platform.AVFoundation.currentItem
import platform.AVFoundation.currentTime
import platform.AVFoundation.duration
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.seekToTime
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMTimeMakeWithSeconds
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
    showControls: Boolean,
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
    var userPaused by remember(url) { mutableStateOf(false) }
    val effectivelyPlaying = isPlaying && !userPaused

    DisposableEffect(playerItem, isLooping) {
        var observer: NSObjectProtocol? = null
        if (isLooping) {
            observer = NSNotificationCenter.defaultCenter.addObserverForName(
                name = AVPlayerItemDidPlayToEndTimeNotification,
                `object` = playerItem,
                queue = NSOperationQueue.mainQueue,
            ) { _ ->
                player.seekToTime(CMTimeMakeWithSeconds(0.0, 600))
                if (!userPaused) player.play()
            }
        }
        onDispose {
            observer?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
            player.pause()
            playerLayer.player = null
        }
    }

    SideEffect {
        if (effectivelyPlaying) player.play() else player.pause()
    }

    fun seekBy(seconds: Double) {
        val current = CMTimeGetSeconds(player.currentTime())
        if (current.isNaN()) return
        val duration = player.currentItem?.duration?.let { CMTimeGetSeconds(it) } ?: Double.POSITIVE_INFINITY
        val target = (current + seconds).coerceIn(0.0, if (duration.isFinite()) duration else current + seconds)
        player.seekToTime(CMTimeMakeWithSeconds(target, 600))
    }

    Box(modifier = modifier) {
        UIKitView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                val view = UIView()
                view.layer.addSublayer(playerLayer)
                view
            },
            update = { view ->
                playerLayer.frame = view.bounds
            },
        )

        if (showControls) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 28.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ControlChip(label = "-10s") { seekBy(-10.0) }
                ControlChip(label = if (effectivelyPlaying) "Pause" else "Play") {
                    userPaused = !userPaused
                }
                ControlChip(label = "+10s") { seekBy(10.0) }
            }
        }
    }
}

@Composable
private fun ControlChip(
    label: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
