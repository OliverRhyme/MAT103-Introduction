package dev.rhyme.introduction.util

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.flowWithLifecycle
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Collects values from this [Flow] and represents its latest value via [State]. Every time there
 * would be new value posted into the [Flow] the returned [State] will be updated causing
 * recomposition of every [State.value] usage. This automatically stops collection when current
 * lifecycle state is less than [minActiveState]
 *
 * @param initial initial data
 * @param context [CoroutineContext] to use for collecting.
 * @param minActiveState [Lifecycle.State] in which the upstream flow gets collected.
 * The collection will stop if the lifecycle falls below that state,
 * and will restart if it's in that state again.
 */
@Composable
fun <T> Flow<T>.collectAsLifecycleState(
    initial: T,
    context: CoroutineContext = EmptyCoroutineContext,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED
): State<T> {
    val lifecycleOwner = LocalLifecycleOwner.current
    val flowLifecycleAware = remember(this, lifecycleOwner, minActiveState) {
        flowWithLifecycle(lifecycleOwner.lifecycle, minActiveState)
    }

    return flowLifecycleAware.collectAsState(initial = initial, context)
}

/**
 * Composable that emits the current lifecycle
 *
 * @param lifecycle lifecycle to observe for changes
 *
 * @return current lifecycle state
 */
@Composable
fun rememberLifecycleState(
    lifecycle: Lifecycle = LocalLifecycleOwner.current.lifecycle
): State<Lifecycle.State> {

    val state = produceState(initialValue = lifecycle.currentState) {
        val observer = LifecycleEventObserver { _, event ->
            value = event.targetState
        }
        lifecycle.addObserver(observer)

        awaitDispose {
            lifecycle.removeObserver(observer)
        }
    }

    return state
}

/**
 * Extension function to easily add padding values
 */
operator fun PaddingValues.plus(other: PaddingValues): PaddingValues {
    return object : PaddingValues {
        override fun calculateBottomPadding(): Dp =
            this@plus.calculateBottomPadding() + other.calculateBottomPadding()

        override fun calculateLeftPadding(layoutDirection: LayoutDirection): Dp =
            this@plus.calculateLeftPadding(layoutDirection) + other.calculateLeftPadding(
                layoutDirection
            )

        override fun calculateRightPadding(layoutDirection: LayoutDirection): Dp =
            this@plus.calculateRightPadding(layoutDirection) + other.calculateRightPadding(
                layoutDirection
            )

        override fun calculateTopPadding(): Dp =
            this@plus.calculateTopPadding() + other.calculateTopPadding()
    }
}