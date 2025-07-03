package com.example.notepad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                TodoApp()
            }
        }
    }
}

@Composable
fun TodoApp(viewModel: TodoViewModel = viewModel()) {
    val todos by viewModel.todos.collectAsState()
    var title by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }
    var editingId by remember { mutableStateOf<Int?>(null) }

    Column(modifier = Modifier.padding(26.dp)) {
        Text("新增/編輯待辦", style = MaterialTheme.typography.titleLarge)
        Text("標題：", style = MaterialTheme.typography.titleMedium)
        BasicTextField(value = title, onValueChange = { title = it }, modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp))
        Text("期限：", style = MaterialTheme.typography.titleMedium)
        BasicTextField(value = deadline, onValueChange = { deadline = it }, modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp))

        Button(onClick = {
            if (editingId == null) {
                viewModel.addTodo(title, deadline)
            } else {
                viewModel.editTodo(editingId!!, title, deadline)
                editingId = null
            }
            title = ""
            deadline = ""
        }) {
            Text(if (editingId == null) "新增" else "儲存")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("待辦清單", style = MaterialTheme.typography.titleMedium)

        // ✅ 這裡改用 LazyColumn 可滑動
        LazyColumn(modifier = Modifier.fillMaxHeight()) {
            items(todos) { todo ->
                TodoItemRow(
                    todo = todo,
                    onEdit = {
                        title = it.title
                        deadline = it.deadline
                        editingId = it.id
                    },
                    onDelete = { viewModel.deleteTodo(it) }
                )
            }
        }
    }
}

class TodoViewModel : ViewModel() {
    private val _todos = MutableStateFlow<List<Todo>>(emptyList())
    val todos = _todos.asStateFlow()

    private var nextId = 1

    fun addTodo(title: String, deadline: String) {
        val newTodo = Todo(nextId++, title, deadline)
        _todos.value = (_todos.value + newTodo).sortedBy { it.deadline }
    }

    fun editTodo(id: Int, title: String, deadline: String) {
        _todos.value = _todos.value.map {
            if (it.id == id) it.copy(title = title, deadline = deadline) else it
        }.sortedBy { it.deadline }
    }

    fun deleteTodo(id: Int) {
        _todos.value = _todos.value.filterNot { it.id == id }
    }
}

data class Todo(
    val id: Int,
    var title: String,
    var deadline: String // 可進一步改為 LocalDate
)

@Composable
fun TodoItemRow(todo: Todo, onEdit: (Todo) -> Unit, onDelete: (Int) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Column {
            Text("標題: ${todo.title}")
            Text("期限: ${todo.deadline}")
        }
        Row {
            Button(onClick = { onEdit(todo) }, modifier = Modifier.padding(end = 4.dp)) {
                Text("編輯")
            }
            Button(onClick = { onDelete(todo.id) }) {
                Text("刪除")
            }
        }
    }
}