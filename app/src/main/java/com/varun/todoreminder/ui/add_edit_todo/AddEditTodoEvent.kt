package com.varun.todoreminder.ui.add_edit_todo

sealed class AddEditTodoEvent {
    data class OnTitleChange(val title: String) : AddEditTodoEvent()
    data class OnDescriptionChange(val description: String) : AddEditTodoEvent()
    data class OnDateChange(val date: String) : AddEditTodoEvent()
    data class OnTimeChange(val time: String) : AddEditTodoEvent()
    data class OnRecurrenceChange(val recurrence: String) : AddEditTodoEvent()
    object OnSaveTodoClick : AddEditTodoEvent()
}
