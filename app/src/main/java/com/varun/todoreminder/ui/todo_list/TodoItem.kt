package com.varun.todoreminder.ui.todo_list

import CircularCheckbox
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.varun.todoreminder.data.local.Todo
import com.varun.todoreminder.ui.theme.CardColor
import com.varun.todoreminder.ui.theme.LabelBlue
import com.varun.todoreminder.ui.theme.LabelGreen
import com.varun.todoreminder.ui.theme.cardShape

@Composable
fun TodoItem(
    viewModel: TodoListViewModel,
    todo: Todo,
    onEvent: (TodoListEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isSpeakingValue by viewModel.speakingState.collectAsState()
    val speakingID by viewModel.speakingIDState.collectAsState()
    var icon = Icons.Filled.PlayArrow
    var isSpeaking = false

    if (speakingID != null) {
        icon = if (isSpeakingValue && speakingID == todo.id) {
            isSpeaking = true
            Icons.Filled.Refresh
        } else {
            isSpeaking = false
            Icons.Filled.PlayArrow
        }
    }

    Card(
        colors = CardColors(
            containerColor = CardColor,
            contentColor = Color.White,
            disabledContainerColor = Color.White,
            disabledContentColor = Color.White
        ),
        modifier = Modifier.padding(16.dp),
        shape = cardShape,
    ) {
        Column {
            if (todo.isRemote) {
                Surface(
                    color = LabelBlue,
                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Online Data",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )
                }
            } else {
                Surface(
                    color = LabelGreen,
                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Offline Data",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )
                }
            }

            Row(
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularCheckbox(
                    modifier = Modifier.padding(all = 12.dp),
                    checked = todo.completed,
                    onCheckedChange = { isChecked ->
                        onEvent(TodoListEvent.OnDoneChange(todo, isChecked))
                    })
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = todo.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    todo.description?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = it)
                    }
                }

                if (!todo.isRemote) {
                    IconButton(onClick = {
                        onEvent(TodoListEvent.OnDeleteTodoClick(todo))
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete"
                        )
                    }
                }

                IconButton(onClick = {
                    if (isSpeaking) {
                        onEvent(
                            TodoListEvent.OnPauseClick(
                                id = todo.id!!,
                                title = todo.title,
                                description = todo.description
                            )
                        )
                    } else {
                        onEvent(
                            TodoListEvent.OnPlayClick(
                                id = todo.id!!,
                                title = todo.title,
                                description = todo.description
                            )
                        )
                    }
                }) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "Play / Stop"
                    )
                }
            }
        }
    }
}