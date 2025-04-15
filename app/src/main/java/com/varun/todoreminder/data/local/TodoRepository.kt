package com.varun.todoreminder.data.local

import com.varun.todoreminder.data.remote.models.TodoDataItem
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

interface TodoRepository {
    suspend fun insertTodo(todo: Todo)

    suspend fun deleteTodo(todo: Todo)

    suspend fun getTodoById(id: Int): Todo?

    fun getTodos(): Flow<List<Todo>>

    suspend fun getTodoFromAPI(): Response<List<TodoDataItem>>

    fun getCombinedTodos(): Flow<List<Todo>>
}