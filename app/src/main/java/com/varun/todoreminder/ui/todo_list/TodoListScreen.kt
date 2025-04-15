package com.varun.todoreminder.ui.todo_list

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.varun.todoreminder.ui.theme.PrimaryColor
import com.varun.todoreminder.util.UiEvent
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(
    onNavigate: (UiEvent.Navigate) -> Unit,
    viewModel: TodoListViewModel = hiltViewModel()
) {
    val activity = LocalActivity.current
    SideEffect {
        val window = activity?.window
        val insetsController = window?.let { WindowInsetsControllerCompat(it, window.decorView) }
        if (insetsController != null) {
            insetsController.isAppearanceLightStatusBars = true
        }
    }
    val todos = viewModel.todos.collectAsState(initial = emptyList())
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val isLoading = viewModel.isLoading.collectAsState()

    val errorState = viewModel.errorState.collectAsState()

    var tts: TextToSpeech? by remember { mutableStateOf(null) }

    LaunchedEffect(key1 = true) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts!!.setLanguage(Locale.US)
                tts!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onDone(utteranceId: String?) {
                        viewModel.updateSpeakingState(false)
                    }

                    override fun onError(utteranceId: String?) {
                        viewModel.updateSpeakingState(false)
                    }

                    override fun onStart(utteranceId: String?) {
                        viewModel.updateSpeakingState(true)
                    }
                })
            } else {
            }
        }
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = event.action
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        if (event.message == "Failed to load Online todos" ||
                            event.message.startsWith("Network error:")
                        ) {
                            viewModel.onEvent(TodoListEvent.OnRefreshTodos)
                        } else {
                            viewModel.onEvent(TodoListEvent.OnUndoDeleteClick)
                        }
                    }
                }

                is UiEvent.Navigate -> onNavigate(event)
                is UiEvent.PlayText -> {
                    viewModel.updateSpeakingIDState(event.id)
                    try {
                        tts?.stop()
                        tts?.speak(
                            "Title : ${event.title + if (event.description != null && event.description != "") ".... Description : " + event.description else ""}",
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            "tts_utterance"
                        )
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar(
                            message = "Unable play audio",
                        )
                    }
                }

                is UiEvent.StopPlaying -> {
                    viewModel.updateSpeakingIDState(null)
                    tts?.stop()
                }

                else -> Unit
            }
        }
    }

    if (isLoading.value) {
        Dialog(
            onDismissRequest = { }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = PrimaryColor,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Please wait, loading...",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }

    if (errorState.value != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            icon = { Icon(Icons.Filled.Warning, contentDescription = "Error") },
            title = { Text("Network Error") },
            text = {
                Text(
                    text = errorState.value ?: "Unable to connect to the server",
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearError()
                        viewModel.onEvent(TodoListEvent.OnRefreshTodos)
                    }
                ) {
                    Text("Retry")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.clearError() }
                ) {
                    Text("Dismiss")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Your To Do", fontWeight = FontWeight.Bold) },
                modifier = Modifier.fillMaxWidth()
            )
        },
        floatingActionButton = {
            Row {
                FloatingActionButton(
                    onClick = {
                        viewModel.onEvent(TodoListEvent.OnRefreshTodos)
                    },
                    shape = CircleShape,
                    containerColor = Color(0xFFDCECFF),
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = Color.Black
                    )
                }

                FloatingActionButton(
                    onClick = {
                        viewModel.onEvent(TodoListEvent.OnAddTodoClick)
                    },
                    shape = CircleShape,
                    containerColor = Color(0xFFDCECFF),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = Color.Black
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val localTodos = todos.value.filter { !it.isRemote }
            val remoteTodos = todos.value.filter { it.isRemote }

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                if (localTodos.isNotEmpty()) {
                    item {
                        Text(
                            text = "Offline Todos",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    items(localTodos) { todo ->
                        TodoItem(
                            viewModel = viewModel,
                            todo = todo,
                            onEvent = viewModel::onEvent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.onEvent(TodoListEvent.OnTodoClick(todo))
                                }
                                .padding(vertical = 16.dp, horizontal = 5.dp),
                        )
                    }
                }

                if (remoteTodos.isNotEmpty()) {
                    item {
                        Text(
                            text = "Online Todos",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    items(remoteTodos) { todo ->
                        TodoItem(
                            viewModel = viewModel,
                            todo = todo,
                            onEvent = viewModel::onEvent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.onEvent(TodoListEvent.OnTodoClick(todo))
                                }
                                .padding(vertical = 16.dp, horizontal = 5.dp),
                        )
                    }
                }

                if (todos.value.isEmpty() && !isLoading.value) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No todos available. Add a todo or refresh to load Online todos.")
                        }
                    }
                }
            }
        }
    }
}