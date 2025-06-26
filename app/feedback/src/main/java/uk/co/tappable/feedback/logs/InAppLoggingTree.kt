@file:OptIn(DelicateCoroutinesApi::class)

package uk.co.tappable.feedback.logs

import android.util.Log
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.util.Date

class InAppLoggingTree(
    private val writeOnFile: Boolean = false,
    private val cacheFile: File? = null,
    private val maxEntries: Int = 200
) :
    Timber.Tree() {


    private val logsBuffer = Channel<LogEvent>()

    private val _buffer = MutableStateFlow<List<LogEvent>>(emptyList())
    val buffer: Flow<List<LogEvent>>
        get() = _buffer
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + createJob())

    private fun createJob(): Job = SupervisorJob()

    init {
        assert(writeOnFile == (cacheFile != null)) { "writeOnFile and cacheFile values do not match " }
        if (cacheFile != null && !cacheFile.exists()) {
            cacheFile.createNewFile()
        }
        coroutineScope.launch {
            while (!logsBuffer.isClosedForReceive) {
                onNewValue(logsBuffer.receive())
            }
        }
    }


    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        logsBuffer.trySend(LogEvent(priority, tag, message, t))
    }

    private fun onNewValue(value: LogEvent) {
        //print logs
        if (writeOnFile) {
            cacheFile?.appendText("\n$value")
        }
        val newList = if ((_buffer.value.size + 1) > maxEntries) {
            _buffer.value.drop(1)
        } else _buffer.value
        _buffer.tryEmit(newList + value)
    }


    private fun wrapTextIntoBlock(title: String, text: String): String {
        return """$title:
            $text
            """.trimIndent()
    }
}

data class LogEvent(
    val priority: Int,
    val tag: String?,
    val message: String,
    val t: Throwable? = null,
    val timestamp: Date = Date(),
) {
    override fun toString(): String {
        return "${priorityToChar(priority)}: ${tag?.let { "($it) " }.orEmpty()}$message"
    }

    private fun priorityToChar(priority: Int): Char = when (priority) {
        Log.DEBUG -> 'D'
        Log.ERROR -> 'E'
        Log.ASSERT -> 'A'
        Log.INFO -> 'I'
        Log.VERBOSE -> 'V'
        Log.WARN -> 'W'
        else -> 'U'
    }

    val color: Color
        get() = when (priority) {
            Log.DEBUG -> Color.Green
            Log.INFO -> Color.Blue
            Log.WARN -> Color.Yellow
            Log.ERROR -> Color.Red
            else -> Color.Black
        }
}