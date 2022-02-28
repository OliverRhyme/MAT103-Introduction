package dev.rhyme.introduction.ui

import android.net.Uri
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.systemBarsPadding
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dev.rhyme.introduction.R
import dev.rhyme.introduction.ui.component.ExoPlayer
import dev.rhyme.introduction.ui.theme.PagpapakilalaTheme
import dev.rhyme.introduction.util.Constants
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlin.random.Random

private const val PAGE_COUNT = 7

@ExperimentalCoroutinesApi
@ExperimentalPagerApi
@Composable
fun IntroductionAppContent() {
    // Remember a SystemUiController
    val systemUiController = rememberSystemUiController()

    val pagerState = rememberPagerState()


    val colors: List<Color> = rememberSaveable(
        PAGE_COUNT,
        saver = listSaver(
            save = { list ->
                list.map { it.value.toLong() }
            }, restore = { list ->
                list.map { Color(it.toULong()) }
            }
        )
    ) {
        buildList {
            repeat(PAGE_COUNT) {
                add(
                    Color(
                        red = Random.nextInt(256),
                        green = Random.nextInt(256),
                        blue = Random.nextInt(256)
                    )
                )
            }
        }
    }

    val bgColor by animateColorAsState(
        targetValue = colors[pagerState.targetPage]
    )
    val isDark = isDarkColor(bgColor)

    PagpapakilalaTheme(darkTheme = !isDark) {
        ProvideWindowInsets {
            val isLight = MaterialTheme.colors.isLight
            SideEffect {
                systemUiController.setSystemBarsColor(
                    color = Color.Transparent,
                    darkIcons = isLight,
                    isNavigationBarContrastEnforced = false
                )
            }

            Scaffold(
                backgroundColor = bgColor,
                contentColor = MaterialTheme.colors.onSurface
            ) {

                Column(
                    modifier = Modifier
                        .systemBarsPadding()
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {

                    ExoPlayer(
                        modifier = Modifier
                            .padding(16.dp)
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(4.dp))
                            .shadow(elevation = 8.dp),
                        playImmediately = true,
                        url = Constants.VIDEO_URL,
                        onError = {
                            throw it
                        },
                        onTimeChanged = { current, total ->

                            val target = when (current) {
                                in 0..5000 -> 0
                                in 5001..17000 -> 1
                                in 17001..35000 -> 2
                                in 35001..79000 -> 3
                                in 79001..total -> 4
                                else -> 0
                            }

                            Log.d("TEST", "target: $target")
                            if (target != pagerState.currentPage) {
                                Log.d("TEST", "moving")
                                pagerState.animateScrollToPage(target)
                            }
                        }
                    )

                    HorizontalPager(
                        modifier = Modifier.weight(1f),
                        count = PAGE_COUNT,
                        state = pagerState
                    ) { page ->
                        when (page) {
                            0 -> CoverPage(modifier = Modifier.fillMaxSize())
                            1 -> SecondPage(modifier = Modifier.fillMaxSize())
                            2 -> ThirdPage(modifier = Modifier.fillMaxSize())
                            3 -> FourthPage(modifier = Modifier.fillMaxSize())
                            4 -> FifthPage(modifier = Modifier.fillMaxSize())
                            5 -> SixthPage(modifier = Modifier.fillMaxSize())
                            6 -> LastPage(modifier = Modifier.fillMaxSize())
                            else -> Box(Modifier.fillMaxSize())
                        }
                    }
                    BottomNav(pagerState = pagerState)
                }

            }
        }
    }
}

fun isDarkColor(color: Color): Boolean {
    val whiteContrast = ColorUtils.calculateContrast(Color.White.toArgb(), color.toArgb())
    val blackContrast = ColorUtils.calculateContrast(Color.Black.toArgb(), color.toArgb())

    return whiteContrast < blackContrast
}

@Composable
fun Page(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(state = rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        content = content,
    )
}

@Composable
fun CoverPage(modifier: Modifier = Modifier) {
    Page(
        modifier = modifier,
    ) {
        Text(
            text = "Hi!", style = MaterialTheme.typography.h3,
            textAlign = TextAlign.Center
        )
        Text(
            text = "It's nice to meet you!", style = MaterialTheme.typography.h4,
            textAlign = TextAlign.Center
        )
        Text(
            text = "I am Oliver Rhyme G. Añasco",
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Image(
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape),
            painter = painterResource(id = R.drawable.oliver),
            contentDescription = "Oliver",
        )
    }
}

@Composable
fun SecondPage(
    modifier: Modifier = Modifier
) {
    Page(modifier = modifier) {
        Text(
            "Let me introduce myself!",
            style = MaterialTheme.typography.h4,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Image(
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape),
            painter = painterResource(id = R.drawable.tagoloan_1),
            contentDescription = "Oliver",
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Im 20 year old student, company co-founder, chief technological officer, future computer engineer and a son. " +
                    "I am currently living in Tagoloan, Misamis Oriental which is famous for our tallest standing Christmas tree in the whole Mindanao.",
            textAlign = TextAlign.Start
        )
    }
}

@Composable
fun ThirdPage(
    modifier: Modifier = Modifier,
) {
    Page(modifier = modifier) {
        Text(
            modifier = Modifier.align(Alignment.Start),
            text = "Just a bit of a background", style = MaterialTheme.typography.h4
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "I am a 2nd year BS in Computer Engineering student at " +
                    "Mindanao State University - Iligan Institute of Technology."
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "I am also a Capitol University Senior High School alumnus of the strand STEM - Engineering."
        )
    }
}

@Composable
fun FourthPage(
    modifier: Modifier = Modifier
) {
    Page(modifier = modifier) {
        Text(
            modifier = Modifier.align(Alignment.Start),
            text = "What particular previous math topic/lesson where you find it difficult to understand?",
            style = MaterialTheme.typography.h4
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "I find calculus to be very challenging especially now that we are not having face to face classes. I have a hard time understanding the topics especially the integrals and also the derivatives. I tried solving on my own, looking up on youtube but still I am not able to fully grasp the idea."
        )
    }
}

@Composable
fun FifthPage(
    modifier: Modifier = Modifier
) {
    Page(modifier = modifier) {
        Text(
            modifier = Modifier.align(Alignment.Start),
            text = "What particular previous math topic/lesson where you enjoy the most?",
            style = MaterialTheme.typography.h4
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "I mostly enjoy the logic related topics on math as it is very close to my line of work which is an Android Developer. I find it very useful as we are always dealing with concrete and discrete logical operations. Having a good knowledge with the topic helps me greatly optimize my work and also be able to design systems that are readable but still very performant. Logic helps me also in my day to day live that it become a no-brainer to me, but if you try to dig deeper, there is more to unveil."
        )
    }
}

@Composable
fun SixthPage(
    modifier: Modifier = Modifier
) {
    Page(modifier = modifier) {
        Text(
            modifier = Modifier.align(Alignment.Start),
            text = "So what's with this app?", style = MaterialTheme.typography.h4
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "I created this app to showcase my greatest passion with programming and creating apps that help people." +
                    "I treat programming and technology in general as my go-to \"stress reliever\" " +
                    "and I feel I am in my safe space by doing the things I love the most."
        )
    }
}

@Composable
fun LastPage(
    modifier: Modifier = Modifier
) {
    Page(modifier = modifier) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(16.dp)
                    .offset(y = (-4).dp),
                painter = painterResource(id = R.drawable.msu_iit),
                contentDescription = "MSU-IIT",
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
//            modifier = Modifier.align(Alignment.Start),
                text = "Maraming salamat sa iyong pakikinig!",
                style = MaterialTheme.typography.h4,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Submitted by: Añasco, Oliver Rhyme G.")
            Text(text = "Submitted to: Ligawon, Elgie T.")
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "MAT103 A4", fontStyle = FontStyle.Italic)

        }

        val context = LocalContext.current

        OutlinedButton(
            onClick = {
                val tabsIntent = CustomTabsIntent.Builder().build()
                tabsIntent.launchUrl(context, Uri.parse(Constants.SOURCE_CODE_URL))
            },
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colors.onSurface,
            )
        ) {
            Text("Tignan ang source code")
        }
    }
}

@ExperimentalPagerApi
@Composable
fun BottomNav(
    modifier: Modifier = Modifier,
    pagerState: PagerState
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val shouldShowBack = pagerState.targetPage != 0
        val shouldShowForward =
            pagerState.targetPage != pagerState.pageCount - 1

        val animationScope = rememberCoroutineScope()

        val backAlpha: Float by animateFloatAsState(if (shouldShowBack) 1f else 0f)
        val forwardAlpha: Float by animateFloatAsState(if (shouldShowForward) 1f else 0f)

        IconButton(
            modifier = Modifier.alpha(backAlpha),
            enabled = shouldShowBack,
            onClick = {
                animationScope.launch {
                    pagerState.animateScrollToPage(
                        pagerState.targetPage - 1
                    )
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Backward"
            )
        }

        HorizontalPagerIndicator(
            modifier = Modifier.padding(16.dp),
            pagerState = pagerState
        )

        IconButton(
            modifier = Modifier.alpha(forwardAlpha),
            enabled = shouldShowForward,
            onClick = {
                animationScope.launch {
                    pagerState.animateScrollToPage(
                        pagerState.targetPage + 1
                    )
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Forward"
            )
        }
    }
}