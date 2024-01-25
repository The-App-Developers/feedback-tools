package uk.co.tappable.feedbuck

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import timber.log.Timber
import uk.co.tappable.feedback.Feedback
import uk.co.tappable.feedbuck.ui.theme.FeedbackToolTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FeedbackToolTheme {
                Feedback(Modifier.fillMaxSize()) {
                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(it)
                    ) { innerPadding ->
                        Greeting(
                            name = "Android",
                            modifier = Modifier
                                .padding(innerPadding)
                                .pointerInput(Unit) {
                                    detectTapGestures(onLongPress = {
                                        Timber.e(
                                            IllegalArgumentException("Long press not supported"),
                                            "Long pressed "
                                        )
                                    }, onTap = {
//                                        Timber.d("Clicked")
                                        Timber
                                            .tag("Test")
                                            .d("Clicked with tag")
                                    })
                                }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FeedbackToolTheme {
        Greeting("Android")
    }
}