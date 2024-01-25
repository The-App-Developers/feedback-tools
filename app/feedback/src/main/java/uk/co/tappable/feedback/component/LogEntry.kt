package uk.co.tappable.feedback.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uk.co.tappable.feedback.logs.LogEvent

@Composable
fun LogEntry(modifier: Modifier = Modifier, logEvent: LogEvent) {
    Card(Modifier.then(modifier)) {
        Row {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(logEvent.color)
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