package emy.partners.lawapp

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

@Composable
actual fun PlatformVideoPlayer(
    url: String,
    modifier: Modifier,
    isPlaying: Boolean,
    isLooping: Boolean,
) {
    val context = LocalContext.current

    val exoPlayer = remember(url) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
        }
    }

    SideEffect {
        exoPlayer.repeatMode = if (isLooping) {
            Player.REPEAT_MODE_ONE
        } else {
            Player.REPEAT_MODE_OFF
        }
        exoPlayer.playWhenReady = isPlaying
        if (isPlaying && exoPlayer.playbackState == Player.STATE_ENDED) {
            exoPlayer.seekTo(0)
            exoPlayer.play()
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = {
            PlayerView(it).apply {
                player = exoPlayer
                useController = false
            }
        },
        modifier = modifier,
    )
}
