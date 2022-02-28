package dev.rhyme.introduction.util

import androidx.compose.runtime.staticCompositionLocalOf
import com.google.android.exoplayer2.upstream.cache.SimpleCache

val LocalSimpleCache = staticCompositionLocalOf<SimpleCache> {
    throw NotImplementedError("SimpleCache not provided")
}
