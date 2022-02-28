package dev.rhyme.introduction.di

import android.content.Context
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object ExoPlayerModule {

    @Singleton
    @Provides
    fun provideSimpleCache(@ApplicationContext context: Context) = SimpleCache(
        context.cacheDir.resolve("exo-cache"), // To prevent it from removing coil cache
        LeastRecentlyUsedCacheEvictor(100 * 1024 * 1024), // 200MB approx. 4-5 videos
        StandaloneDatabaseProvider(context)
    )
}