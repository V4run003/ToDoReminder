package com.varun.todoreminder.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Todo(
    val title: String,
    val description: String? = null,
    val completed: Boolean = false,
    val date: String? = null,
    val time: String? = null,
    val recurrence: String? = null,
    val isRemote: Boolean = false,
    @PrimaryKey val id: Int? = null
)