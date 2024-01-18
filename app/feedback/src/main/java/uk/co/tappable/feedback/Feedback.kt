package uk.co.tappable.feedback

import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt


private const val minSpacing = 20

@Composable
fun Feedback(modifier: Modifier = Modifier, content: @Composable (Modifier) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val displayMetrics = context.resources.displayMetrics
    val density = LocalDensity.current
    val spacing = remember(context) { with(density) { minSpacing.dp.toPx() } }
    val screenHeight = remember(context) { displayMetrics.heightPixels }
    val screenWidth = remember(context) { displayMetrics.widthPixels }
    var offsetX by rememberSaveable { mutableFloatStateOf(screenWidth / 2f) }
    var offsetY by rememberSaveable { mutableFloatStateOf(screenHeight / 2f) }
    val offset = animateOffsetAsState(targetValue = Offset(offsetX, offsetY), label = "debug")
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
            Box(
                Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .blur(10.dp)
            )
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
                    detectDragGestures(onDrag = { change, dragAmount ->
                        change.consume()

                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }, onDragEnd = {
                        offsetX = if (offsetX <= screenWidth / 2)
                            fabSize.width
                                .div(2)
                                .times(-1)
                                .plus(spacing)
                        else
                            screenWidth.toFloat() - fabSize.width
                                .div(2)
                                .plus(spacing)
                    })
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