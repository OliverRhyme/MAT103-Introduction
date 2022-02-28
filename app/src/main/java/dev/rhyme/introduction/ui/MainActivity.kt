package dev.rhyme.introduction.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.view.WindowCompat
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import dagger.hilt.android.AndroidEntryPoint
import dev.rhyme.introduction.util.LocalSimpleCache
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalPagerApi
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var simpleCache: SimpleCache

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {

            CompositionLocalProvider(LocalSimpleCache provides simpleCache) {
                IntroductionAppContent()
            }
        }
    }
}