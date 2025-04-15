package com.varun.todoreminder.ui.todo_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varun.todoreminder.data.local.Todo
import com.varun.todoreminder.data.local.TodoRepository
import com.varun.todoreminder.util.Routes
import com.varun.todoreminder.util.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import javax.inject.Inject

@HiltViewModel
class TodoListViewModel @Inject constructor(
    private val repository: TodoRepository
) : ViewModel() {

    private val _speakingState = MutableStateFlow(false)
    val speakingState: StateFlow<Boolean> = _speakingState.asStateFlow()

    private val _speakingIDState = MutableStateFlow<Int?>(null)
    val speakingIDState: StateFlow<Int?> = _speakingIDState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState.asStateFlow()

    val todos = repository.getCombinedTodos()

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private var deletedTodo: Todo? = null

    init {
        refreshRemoteTodos()
    }

    fun onEvent(event: TodoListEvent) {
        when (event) {
            is TodoListEvent.OnTodoClick -> {
                if (!event.todo.isRemote) {
                    sendUiEvent(UiEvent.Navigate(Routes.ADD_EDIT_TODO + "?todoId=${event.todo.id}"))
                } else {
                    sendUiEvent(
                        UiEvent.ShowSnackbar(
                            message = "Online todos cannot be edited",
                            action = null
                        )
                    )
                }
            }

            is TodoListEvent.OnPlayClick -> {
                sendUiEvent(
                    UiEvent.PlayText(
                        id = event.id,
                        title = event.title,
                        description = event.description
                    )
                )
            }

            is TodoListEvent.OnPauseClick -> {
                sendUiEvent(
                    UiEvent.StopPlaying(
                        id = event.id,
                        title = event.title,
                        description = event.description
                    )
                )
            }

            is TodoListEvent.OnAddTodoClick -> {
                sendUiEvent(UiEvent.Navigate(Routes.ADD_EDIT_TODO))
            }

            is TodoListEvent.OnUndoDeleteClick -> {
                deletedTodo?.let { todo ->
                    viewModelScope.launch {
                        repository.insertTodo(todo)
                    }
                }
            }

            is TodoListEvent.OnDeleteTodoClick -> {
                if (!event.todo.isRemote) {
                    viewModelScope.launch {
                        deletedTodo = event.todo
                        repository.deleteTodo(event.todo)
                        sendUiEvent(
                            UiEvent.ShowSnackbar(
                                message = "Todo deleted",
                                action = "Undo"
                            )
                        )
                    }
                }
            }

            is TodoListEvent.OnDoneChange -> {
                viewModelScope.launch {
                    if (!event.todo.isRemote) {
                        repository.insertTodo(
                            event.todo.copy(
                                completed = event.completed
                            )
                        )
                    } else {
                        sendUiEvent(
                            UiEvent.ShowSnackbar(
                                message = "Online todo status updated locally only",
                                action = null
                            )
                        )
                    }
                }
            }

            is TodoListEvent.OnRefreshTodos -> {
                refreshRemoteTodos()
            }
        }
    }

    fun updateSpeakingState(isSpeaking: Boolean) {
        _speakingState.value = isSpeaking
    }

    fun updateSpeakingIDState(id: Int?) {
        _speakingIDState.value = id
    }

    fun clearError() {
        _errorState.value = null
    }

    private fun sendUiEvent(event: UiEvent) {
        viewModelScope.launch {
            _uiEvent.send(event)
        }
    }

    private fun refreshRemoteTodos() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = repository.getTodoFromAPI()
                _isLoading.value = false

                if (!response.isSuccessful) {
                    _errorState.value = "Server error: ${response.code()}"
                }
            } catch (e: UnknownHostException) {
                _isLoading.value = false
                _errorState.value = "No internet connection"
            } catch (e: Exception) {
                _isLoading.value = false
                _errorState.value = "Network error: ${e.message}"
            }
        }
    }
}