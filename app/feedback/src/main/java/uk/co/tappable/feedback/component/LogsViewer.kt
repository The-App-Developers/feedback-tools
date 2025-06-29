@file:OptIn(ExperimentalMaterial3Api::class)

package uk.co.tappable.feedback.component

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.map
import timber.log.Timber
import uk.co.tappable.feedback.R
import uk.co.tappable.feedback.logs.InAppLoggingTree
import uk.co.tappable.feedback.logs.LogEvent

@Composable
fun LogsViewer(modifier: Modifier = Modifier, onLogClicked: (LogEvent) -> Unit = {}) {
    val loggingTree: InAppLoggingTree? =
        remember(Unit) {
            Timber.forest().firstOrNull { it is InAppLoggingTree } as InAppLoggingTree?
        }
    if (loggingTree == null) {
        Timber.e("No logging tree found")
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(modifier)
        ) {
            Text(
                text = stringResource(R.string.no_logging_tree_found),
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.headlineSmall
            )
        }
        return
    }
    var selectedFilter: Int? by remember { mutableStateOf(null) }
    var textFilter by remember { mutableStateOf("") }
    val logs =
        loggingTree.buffer.map { list ->
            val filterByLevel = if (selectedFilter == null) list else list.filter {
                it.priority >= selectedFilter!!
            }
            if (textFilter.isNotBlank()) {
                filterByLevel.filter {
                    it.message.contains(
                        textFilter,
                        ignoreCase = true
                    ) || it.tag.orEmpty().contains(textFilter, ignoreCase = true)
                }
            } else filterByLevel
        }.collectAsState(null)
    Column(
        modifier.padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            Modifier.padding(top = 12.dp)
        ) {
            TextField(
                value = textFilter,
                onValueChange = { textFilter = it },
                label = { Text(stringResource(R.string.search)) },
                modifier = Modifier.weight(3f),
            )
            Spacer(modifier = Modifier.width(6.dp))
            LogFilter(
                onFilterSelected = { selectedFilter = it }, selectedFilter = selectedFilter,
                modifier = Modifier.weight(2f)
            )

        }
        Spacer(modifier = Modifier.height(6.dp))
        LazyColumn(
            modifier = modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp)
        ) {
            items(logs.value.orEmpty()) { log ->
                LogEntry(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .animateItem(),
                    onClick = { onLogClicked(log) },
                    logEvent = log
                )
            }
        }
    }
}

@Composable
private fun LogFilter(
    onFilterSelected: (Int?) -> Unit,
    modifier: Modifier = Modifier,
    selectedFilter: Int? = null
) {
    var filterExpanded by remember { mutableStateOf(false) }
    val filters = remember {
        listOf(
            null,
            Log.DEBUG,
            Log.INFO,
            Log.WARN,
            Log.ERROR,
            Log.VERBOSE
        )
    }
    ExposedDropdownMenuBox(
        expanded = filterExpanded,
        onExpandedChange = { filterExpanded = it },
        modifier = Modifier.then(modifier),
    ) {
        TextField(
            value = textFromLogLevel(selectedFilter),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.filter)) },
            modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryEditable),
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = filterExpanded) },
        )
        ExposedDropdownMenu(
            expanded = filterExpanded,
            onDismissRequest = { filterExpanded = false },
            Modifier.exposedDropdownSize()
        ) {
            filters.forEach { filter ->
                DropdownMenuItem(
                    text = {
                        Text(
                            textFromLogLevel(filter),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    onClick = {
                        onFilterSelected(filter)
                        filterExpanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

fun textFromLogLevel(logLevel: Int?): String {
    return when (logLevel) {
        Log.DEBUG -> "Debug"
        Log.INFO -> "Info"
        Log.WARN -> "Warn"
        Log.ERROR -> "Error"
        Log.VERBOSE -> "Verbose"
        else -> "All"
    }
}