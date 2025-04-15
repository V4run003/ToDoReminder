package com.varun.todoreminder.di

import android.app.Application
import androidx.room.Room
import com.varun.todoreminder.data.local.TodoDatabase
import com.varun.todoreminder.data.local.TodoRepository
import com.varun.todoreminder.data.local.TodoRepositoryImpl
import com.varun.todoreminder.data.remote.api.TodoAPI
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTodoDatabase(app: Application): TodoDatabase {
        return Room.databaseBuilder(
            app,
            TodoDatabase::class.java,
            "todo_db"
        ).build()
    }


    @Provides
    @Singleton
    fun providesRetrofit(): Retrofit {
        return Retrofit.Builder().baseUrl("https://jsonplaceholder.typicode.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun providesAPI(retrofit: Retrofit): TodoAPI {
        return retrofit.create(TodoAPI::class.java)

    }

    @Provides
    @Singleton
    fun provideTodoRepository(db: TodoDatabase, api: TodoAPI): TodoRepository {
        return TodoRepositoryImpl(db.dao, api)
    }

}