package com.varun.todoreminder.data.remote.models

data class TodoDataItem(
    val completed: Boolean,
    val id: Int,
    val title: String,
    val userId: Int,
    val description : String?
)