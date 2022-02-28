package dev.rhyme.introduction.ui.component

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import dev.rhyme.introduction.util.LocalSimpleCache
import dev.rhyme.introduction.util.rememberLifecycleState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlin.time.Duration

/**
 * Exoplayer composable wrapper, tailor made for preview video
 *
 * @param url url of the video to play
 * @param playImmediately play immediately on player attach
 * @param onShowControls show controls callback
 * @param onError playback error callback to handle errors
 */
@ExperimentalCoroutinesApi
@Composable
fun ExoPlayer(
    modifier: Modifier = Modifier,
    url: String,
    playImmediately: Boolean = false,
    onShowControls: (Boolean) -> Unit = {},
    onTimeChanged: (suspend (Long, Long) -> Unit)? = null,
    onError: ((PlaybackException) -> Unit)? = null
) {
    val context = LocalContext.current

    val exoPlayer = remember(context) {
        com.google.android.exoplayer2.ExoPlayer.Builder(context)
            .build()
    }

    val cache = LocalSimpleCache.current

    DisposableEffect(url, context, cache) {
        val mediaItem = MediaItem.Builder()
            .setUri(url)
            .build()

        // Data source chaining
        val httpDataSource = DefaultHttpDataSource.Factory()
        val dataSource = DefaultDataSource.Factory(context, httpDataSource)
        val cacheDataSource = CacheDataSource.Factory().apply {
            setCache(cache)
            setUpstreamDataSourceFactory(dataSource)
            setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR) // We fetch on upstream when cache errors
        }

        val mediaSource = ProgressiveMediaSource.Factory(cacheDataSource)
            .createMediaSource(mediaItem)

        exoPlayer.setMediaSource(mediaSource)
        exoPlayer.prepare()
        onDispose {
            exoPlayer.release()
        }
    }

    // We pause the player when we are at least resumed (screen is visible)
    val lifecycle by rememberLifecycleState()

    LaunchedEffect(lifecycle) {
        if (!lifecycle.isAtLeast(Lifecycle.State.RESUMED)) {
            exoPlayer.pause()
        }
    }

    if (onError != null) {
        DisposableEffect(onError, exoPlayer) {
            val errorCallback = object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    onError(error)
                }
            }

            exoPlayer.addListener(errorCallback)

            onDispose {
                exoPlayer.removeListener(errorCallback)
            }
        }
    }

    if (onTimeChanged != null) {
        LaunchedEffect(onTimeChanged, exoPlayer) {

            callbackFlow<Boolean> {
                val playbackCallback = object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        super.onIsPlayingChanged(isPlaying)
                        trySendBlocking(isPlaying)
                    }
                }

                exoPlayer.addListener(playbackCallback)

                awaitClose { exoPlayer.removeListener(playbackCallback) }
            }.flatMapLatest {
                if (it) {
                    flow {
                        while (coroutineContext.isActive) {
                            emit(exoPlayer.currentPosition to exoPlayer.duration)
                            delay(500)
                        }
                    }
                } else {
                    emptyFlow()
                }
            }.collect { (current, total) ->
                onTimeChanged(current, total)
            }
        }
    }

    AndroidView(
        modifier = modifier,
        factory = {

            if (playImmediately) {
                exoPlayer.play()
            }

            StyledPlayerView(it).apply {
                player = exoPlayer

                setShowNextButton(false)
                setShowPreviousButton(false)

                controllerAutoShow = false

                setShowBuffering(StyledPlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                setControllerVisibilityListener { visibility ->
                    onShowControls(visibility == View.VISIBLE)
                }
            }
        }
    )
}

fun tickerFlow(period: Duration, initialDelay: Duration = Duration.ZERO) = flow {
    delay(initialDelay)
    while (true) {
        emit(Unit)
        delay(period)
    }
}