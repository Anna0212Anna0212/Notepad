package com.example.notepad.ui.theme.VIewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.notepad.ui.theme.Room.TodoDatabase
import com.example.notepad.ui.theme.Room.TodoEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TodoViewModel(application: Application) : AndroidViewModel(application) {//TodoViewModel error
private val todoDao = TodoDatabase.getDatabase(application).todoDao()
    private val dao = TodoDatabase.getDatabase(application).todoDao()
    val todos: Flow<List<TodoEntity>> = todoDao.getAllTodosSortedByEndDate()

    fun addTodo(title: String, startDate: String,endDate:String) {
        viewModelScope.launch {
            todoDao.insert(TodoEntity(title = title, startDate = startDate, endDate = endDate))
        }
    }

    fun deleteTodo(todo: TodoEntity) {
        viewModelScope.launch {
            todoDao.delete(todo)
        }
    }

    fun editTodo(todo: TodoEntity) {
        viewModelScope.launch {
            todoDao.update(todo)
        }
    }
}