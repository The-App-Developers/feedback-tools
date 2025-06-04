package uk.co.tappable.feedback.component

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun InAppActions(
    modifier: Modifier = Modifier,
    actions: List<Action>,
    onClick: (Action) -> Unit,
) {
    LazyColumn(
        modifier.scrollable(
            rememberScrollState(),
            orientation = Orientation.Vertical,
        ),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = WindowInsets.safeContent.asPaddingValues()
    ) {
        items(actions) {
            ActionMenuItem(
                onClick = { onClick(it) },
                action = it,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(60.dp)
            )
        }
    }
}

@Composable
fun ActionMenuItem(modifier: Modifier = Modifier, onClick: () -> Unit, action: Action) {
    when (action) {
        is Action.Click -> ClickableAction(modifier, onClick, action)
        is Action.Toggle -> ToggleAction(modifier, onClick, action)
    }
}

@Composable
fun ClickableAction(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    action: Action.Click
) {
    ElevatedButton(onClick, modifier) {
        Text(action.title)
    }
}

@Composable
fun ToggleAction(modifier: Modifier = Modifier, onClick: () -> Unit, action: Action.Toggle) {
    TextButton(
        onClick = onClick,
        modifier = modifier.toggleable(
            value = action.checked,
            onValueChange = { },
            role = Role.Checkbox,
        ),
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = if (action.checked) MaterialTheme.colorScheme.surfaceContainer
            else MaterialTheme.colorScheme.onSurface,
            contentColor = if (action.checked) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Text(action.title)
    }
}

@Preview
@Composable
private fun ActionMenuItem_Preview() {
    ActionMenuItem(action = Action.Click(title = "Test"), onClick = {})
}

@Preview
@Composable
private fun ActionMenu_Toggle_Preview() {
    ActionMenuItem(
        action = Action.Toggle(title = "Test", checked = true),
        onClick = {}
    )
}


sealed class Action(
    open val id: Int = 0,
    open val title: String,
) {
    data class Click(override val title: String, override val id: Int = 0) :
        Action(title = title, id = id)

    data class Toggle(
        override val title: String,
        override val id: Int = 0,
        val checked: Boolean = false
    ) :
        Action(title = title, id = id)

}