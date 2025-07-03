package com.example.notepad

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

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

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, day: Int ->
            deadline = "$year-${month + 1}-$day"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(26.dp)
    ) {
        // 🟢 表單區（固定在上）
        Text("新增/編輯待辦", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("標題") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )

        OutlinedTextField(
            value = deadline,
            onValueChange = {},
            label = { Text("期限") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { datePickerDialog.show() }) {
                    Icon(Icons.Default.DateRange, contentDescription = "選擇日期")
                }
            }
        )

        Button(
            onClick = {
                if (title.isBlank()) {
                    Toast.makeText(context, "請輸入標題", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (editingId == null) {
                    viewModel.addTodo(title, deadline)
                } else {
                    viewModel.editTodo(editingId!!, title, deadline)
                    editingId = null
                }
                title = ""
                deadline = ""
            },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(if (editingId == null) "新增" else "儲存")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("待辦清單", style = MaterialTheme.typography.titleMedium)

        if (todos.isEmpty()) {
            Text("尚無待辦事項，請新增一筆。", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
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
}


//儲存資料
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 左側：標題與期限
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text("標題: ${todo.title}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(4.dp))
                Text("期限: ${todo.deadline}", style = MaterialTheme.typography.bodyMedium)
            }

            // 右側：編輯與刪除按鈕
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { onEdit(todo) },
                    modifier = Modifier.padding(end = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("編輯")
                }

                Button(
                    onClick = { onDelete(todo.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("刪除")
                }
            }
        }
    }
}
