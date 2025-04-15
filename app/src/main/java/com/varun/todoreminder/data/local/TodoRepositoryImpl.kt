package com.varun.todoreminder.data.local

import com.varun.todoreminder.data.remote.api.TodoAPI
import com.varun.todoreminder.data.remote.models.TodoDataItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import retrofit2.Response

class TodoRepositoryImpl(
    private val dao: TodoDao,
    private val api: TodoAPI
) : TodoRepository {

    private val _todos = MutableStateFlow<List<TodoDataItem>>(emptyList())
    val todos: StateFlow<List<TodoDataItem>> = _todos

    private val targetUserId = 3

    private fun TodoDataItem.toTodo(): Todo {
        return Todo(
            id = this.id,
            title = this.title,
            description = this.description,
            completed = this.completed,
            isRemote = true
        )
    }

    override suspend fun insertTodo(todo: Todo) {
        dao.insertTodo(todo)
    }

    override suspend fun deleteTodo(todo: Todo) {
        dao.deleteTodo(todo)
    }

    override suspend fun getTodoById(id: Int): Todo? {
        return dao.getTodoById(id)
    }

    override fun getTodos(): Flow<List<Todo>> {
        return dao.getTodos()
    }

    override suspend fun getTodoFromAPI(): Response<List<TodoDataItem>> {
        return api.getTodoList()
    }

    override fun getCombinedTodos(): Flow<List<Todo>> {
        return combine(
            dao.getTodos(),
            fetchAndCacheRemoteTodos()
        ) { localTodos, remoteTodos ->
            localTodos + remoteTodos
        }
    }

    private fun fetchAndCacheRemoteTodos(): Flow<List<Todo>> {
        return flow {
            try {
                val response = api.getTodoList()
                if (response.isSuccessful && response.body() != null) {
                    val filteredRemoteTodos = response.body()!!
                        .filter { it.userId == targetUserId }
                        .map { it.toTodo() }
                    emit(filteredRemoteTodos)
                } else {
                    emit(emptyList())
                }
            } catch (e: Exception) {
                emit(emptyList())
            }
        }
    }
}