package com.varun.todoreminder.ui.todo_list

import com.varun.todoreminder.data.local.Todo

sealed class TodoListEvent {
    data class OnDeleteTodoClick(val todo: Todo) : TodoListEvent()
    data class OnDoneChange(val todo: Todo, val completed: Boolean) : TodoListEvent()
    data class OnTodoClick(val todo: Todo) : TodoListEvent()
    data class OnPlayClick(val id: Int, val title: String, val description: String?) :
        TodoListEvent()

    data class OnPauseClick(val id: Int, val title: String, val description: String?) :
        TodoListEvent()

    object OnAddTodoClick : TodoListEvent()
    object OnUndoDeleteClick : TodoListEvent()
    object OnRefreshTodos : TodoListEvent()
}