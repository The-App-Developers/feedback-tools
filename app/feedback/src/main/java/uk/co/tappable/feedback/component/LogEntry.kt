package uk.co.tappable.feedback.component

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import uk.co.tappable.feedback.logs.LogEvent

@Composable
fun LogEntry(modifier: Modifier = Modifier, onClick: () -> Unit, logEvent: LogEvent) {
    val interactionSource = remember { MutableInteractionSource() }
    var showMenu by remember { mutableStateOf(false) }
    Card(
        modifier.combinedClickable(
            onLongClick = {
                showMenu = true
            },
            onClick = onClick,
            role = Role.Button,
        ),
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .background(logEvent.color)
                    .width(4.dp)
                    .fillMaxHeight()
            )
            Column(
                Modifier
                    .weight(1f)
                    .padding(
                        vertical = 12.dp, horizontal = 8.dp
                    )
                    .height(84.dp)
            ) {
                if (!logEvent.tag.isNullOrBlank()) {
                    Text(
                        logEvent.tag,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = logEvent.message,
                    style = MaterialTheme.typography.bodyMedium,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}