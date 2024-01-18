package uk.co.tappable.feedback

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt


private const val minSpacing = 8

@Composable
fun Feedback(modifier: Modifier = Modifier, content: @Composable (Modifier) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val displayMetrics = context.resources.displayMetrics
    val density = LocalDensity.current
    val spacing = remember(context) { with(density) { minSpacing.dp.toPx() } }
    val screenHeight = remember(context) { displayMetrics.heightPixels }
    val screenWidth = remember(context) { displayMetrics.widthPixels }
    var offsetX by rememberSaveable { mutableFloatStateOf(0f) }
    var offsetY by rememberSaveable { mutableFloatStateOf(0f) }
    val offset = animateOffsetAsState(
        targetValue = Offset(if (expanded) 5f else offsetX, if (expanded) 5f else offsetY),
        label = "debug"
    )
    val blur = animateDpAsState(targetValue = if (expanded) 10.dp else 0.dp, label = "blur")

    var fabSize by remember { mutableStateOf(IntSize(0, 0)) }
    val contentModifier = Modifier.blur(blur.value)
    BoxWithConstraints(modifier = modifier) {
        content(contentModifier)
        AnimatedVisibility(
            expanded,
            modifier = Modifier.matchParentSize(),
            enter = slideInVertically(initialOffsetY = { it + 50 }),
            exit = slideOutVertically(targetOffsetY = { it + 50 }),
        ) {
            //show an full detailed ui with a text field and a button
            Surface(
                modifier = Modifier
                    .statusBarsPadding()
                    .systemBarsPadding()
                    .padding(top = with(density) { (fabSize.height / 2).toDp() })
                    .matchParentSize(),
                color = MaterialTheme.colorScheme.onSurface
            ) {
                //TODO: add content to debug menu
            }
        }
        //show a floating button above the app content

        FloatingActionButton(
            onClick = {
                expanded = !expanded
                println("Feedback button clicked")
            },
            modifier = Modifier
                .onGloballyPositioned {
                    fabSize = it.size
                    if (offset.value == Offset.Zero) {
                        //update position to half screen
                        offsetX = screenWidth.toFloat() / 2
                        offsetY = screenHeight.toFloat() / 2
                    }
                }
                .offset {
                    IntOffset(
                        offset.value.x.roundToInt(),
                        offset.value.y
                            .roundToInt()
                            .coerceIn(fabSize.height, screenHeight)
                    )
                }
                .pointerInput(Unit) {
                    if (!expanded) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()

                                offsetX += dragAmount.x
                                offsetY += dragAmount.y
                            },
                            onDragEnd = {
                                offsetX = offsetX.roundToNearestValue(
                                    lowEnd = fabSize.width
                                        .div(2)
                                        .times(-1)
                                        .plus(spacing),
                                    highEnd = screenWidth.toFloat() - fabSize.width
                                        .div(2)
                                        .plus(spacing)
                                )
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

fun Float.roundToNearestValue(lowEnd: Float, highEnd: Float): Float {
    val average = (lowEnd + highEnd) / 2
    return if (this <= average) lowEnd
    else highEnd
}