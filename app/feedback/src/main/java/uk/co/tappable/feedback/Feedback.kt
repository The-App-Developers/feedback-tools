@file:OptIn(ExperimentalMaterial3Api::class)

package uk.co.tappable.feedback

import android.annotation.SuppressLint
import android.content.ClipData
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.SnapSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.Approval
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.DeveloperMode
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import uk.co.tappable.feedback.component.Action
import uk.co.tappable.feedback.component.InAppActions
import uk.co.tappable.feedback.component.LogsViewer
import kotlin.math.roundToInt


private const val minSpacing = 8

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun Feedback(
    modifier: Modifier = Modifier,
    actions: List<Action>? = null,
    onActionClick: (Action) -> Unit = {},
    content: @Composable (Modifier) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val displayMetrics = context.resources.displayMetrics
    val density = LocalDensity.current
    val spacing = remember(context) { with(density) { minSpacing.dp.toPx() } }
    val screenHeight = remember(context) { displayMetrics.heightPixels }
    val screenWidth = remember(context) { displayMetrics.widthPixels }
    var offsetX by rememberSaveable { mutableFloatStateOf(0f) }
    var offsetY by rememberSaveable { mutableFloatStateOf(0f) }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val offset by animateOffsetAsState(
        targetValue = Offset(if (expanded) 5f else offsetX, if (expanded) 5f else offsetY),
        label = "debugOffsetAnimation",
        animationSpec = spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium)
    )
    val blur = animateDpAsState(targetValue = if (expanded) 10.dp else 0.dp, label = "blur")

    var fabSize by remember { mutableStateOf(IntSize(0, 0)) }
    val contentModifier = Modifier.blur(blur.value)
    BackHandler(enabled = expanded) {
        expanded = false
    }
    BoxWithConstraints(modifier = modifier) {
        content(contentModifier)
        AnimatedVisibility(
            expanded,
            modifier = Modifier.matchParentSize(),
            enter = slideInVertically(
                initialOffsetY = { it + 50 },
                animationSpec = spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMedium)
            ),
            exit = slideOutVertically(targetOffsetY = { it + 50 }, animationSpec = spring()),
        ) {
            //show an full detailed ui with a text field and a button
            Surface(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = with(density) { (fabSize.height / 2).toDp() })
                    .matchParentSize(),
                color = MaterialTheme.colorScheme.onSurface
            ) {
                Column(Modifier.fillMaxSize()) {
                    val tabContentModifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                    when (selectedTab) {
                        1 -> InAppActions(
                            actions = actions.orEmpty(),
                            onClick = onActionClick,
                            modifier = tabContentModifier
                        )

                        else -> {
                            val clipboard = LocalClipboard.current
                            val coroutineScope = rememberCoroutineScope()
                            LogsViewer(tabContentModifier, onLogClicked = { entry ->
                                //copy to clipboard the log message
                                coroutineScope.launch {
                                    clipboard.setClipEntry(
                                        ClipEntry(
                                            ClipData.newPlainText(
                                                entry.message,
                                                listOf(entry.tag, entry.message).joinToString(" - ")
                                            )
                                        )
                                    )
                                }
                            })
                        }
                    }
                    NavigationBar {
                        NavigationBarItem(
                            selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = {
                                Icon(
                                    imageVector = Icons.Rounded.DeveloperMode,
                                    contentDescription = null
                                )
                            },
                            label = { Text(stringResource(R.string.logs)) },
                        )
                        NavigationBarItem(
                            selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = {
                                Icon(
                                    imageVector = Icons.Rounded.BugReport,
                                    contentDescription = null
                                )
                            },
                            label = { Text(stringResource(R.string.actions)) },
                        )
                    }
                }
            }
        }
        //show a floating button above the app content
        BoxWithConstraints(
            Modifier
                .statusBarsPadding()
                .fillMaxSize()
        ) {
            FloatingActionButton(
                onClick = {
                    expanded = !expanded
                },
                modifier = Modifier
                    .onGloballyPositioned {
                        fabSize = it.size
                        if (offset == Offset.Zero) {
                            //update position to half screen
                            offsetX = screenWidth.toFloat() / 2
                            offsetY = screenHeight.toFloat() / 2
                        }
                    }
                    .offset {
                        IntOffset(
                            offset.x.roundToInt(),
                            offset.y.roundToInt()
                                .coerceIn(0, constraints.maxHeight - fabSize.height)
                        )
                    }
                    .pointerInput(Unit) {
                        if (!expanded) {
                            detectDragGestures(
                                onDrag = { change, dragAmount ->
                                    if (!expanded) {
                                        change.consume()

                                        offsetX += dragAmount.x
                                        offsetY += dragAmount.y
                                    }
                                },
                                onDragEnd = {
                                    if (!expanded) {
                                        offsetX = offsetX.roundToNearestValue(
                                            lowEnd = fabSize.width.div(2).times(-1)
                                                .plus(spacing),
                                            highEnd = constraints.maxWidth.toFloat() - fabSize.width.div(
                                                2
                                            ).plus(spacing)
                                        )
                                    }
                                },
                            )
                        }
                    },
                shape = CircleShape,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                content = {
                    Icon(
                        imageVector = if (expanded) Icons.Default.Close else Icons.Default.BugReport,
                        contentDescription = "Feedback",
                    )
                },
            )
        }
    }
}

/**
 * Rounds this Float to the nearest of the two provided boundary values.
 * If the Float is exactly halfway between [lowEnd] and [highEnd], it rounds towards [lowEnd].
 *
 * @param lowEnd The lower boundary value.
 * @param highEnd The higher boundary value.
 * @return [lowEnd] if this Float is closer to or exactly halfway towards [lowEnd], otherwise [highEnd].
 * @throws IllegalArgumentException if [lowEnd] is greater than [highEnd] (consider handling or swapping internally).
 */
fun Float.roundToNearestValue(lowEnd: Float, highEnd: Float): Float {
    val actualLow = minOf(lowEnd, highEnd)
    val actualHigh = maxOf(lowEnd, highEnd)
    val average = (actualLow + actualHigh) / 2
    return if (this <= average) actualLow
    else actualHigh
}