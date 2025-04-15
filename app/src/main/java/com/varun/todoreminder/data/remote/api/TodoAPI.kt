package com.varun.todoreminder.data.remote.api

import com.varun.todoreminder.data.remote.models.TodoDataItem
import retrofit2.Response
import retrofit2.http.GET

interface TodoAPI {

    @GET("/todos")
    suspend fun getTodoList(): Response<List<TodoDataItem>>
}