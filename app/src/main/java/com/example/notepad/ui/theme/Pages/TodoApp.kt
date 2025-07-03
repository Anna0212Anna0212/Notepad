package com.example.notepad.ui.theme.Pages

import android.app.Application
import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notepad.ui.theme.Room.TodoEntity
import com.example.notepad.ui.theme.VIewModel.TodoViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun TodoApp(viewModel: TodoViewModel = viewModel()) {
    val context = LocalContext.current
    val viewModel: TodoViewModel = viewModel(
        factory = object : ViewModelProvider.AndroidViewModelFactory(context.applicationContext as Application) {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TodoViewModel(context.applicationContext as Application) as T
            }
        }
    )
    var title by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }
    var editingId by remember { mutableStateOf<Int?>(null) }
    val todos by viewModel.todos.collectAsState(initial = emptyList())
    val calendar = Calendar.getInstance()
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }

    /*
    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, day: Int ->
            deadline = "$year-${month + 1}-$day"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    */

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(26.dp)
    ) {
        // 🟢 表單區（固定在上）
        Text(
            "新增/編輯待辦",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("標題") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )

        val startDatePickerDialog = DatePickerDialog(
            context,
            { _, year, month, day -> startDate = "$year-${month + 1}-$day" },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        val endDatePickerDialog = DatePickerDialog(
            context,
            { _, year, month, day -> endDate = "$year-${month + 1}-$day" },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        OutlinedTextField(
            value = startDate,
            onValueChange = {},
            label = { Text("開始日期") },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { startDatePickerDialog.show() }) {
                    Icon(Icons.Default.DateRange, contentDescription = "選擇開始日期")
                }
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        )

        OutlinedTextField(
            value = endDate,
            onValueChange = {},
            label = { Text("結束日期") },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { endDatePickerDialog.show() }) {
                    Icon(Icons.Default.DateRange, contentDescription = "選擇結束日期")
                }
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        )

        Button(
            onClick = {
                if (title.isBlank()) {
                    Toast.makeText(context, "請輸入標題", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (startDate.isBlank()) {
                    Toast.makeText(context, "請輸入期限", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (endDate.isBlank()) {
                    Toast.makeText(context, "請輸入期限", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val start = formatter.parse(startDate)
                val end = formatter.parse(endDate)
                if (start != null && end != null && end.before(start)) {
                    Toast.makeText(context, "請輸入正確結束日期", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (editingId == null) {
                    viewModel.addTodo(
                        title, startDate,endDate
                    )//deadline error
                } else {
                    val todo = TodoEntity(editingId!!,title,startDate,endDate )
                    viewModel.editTodo(todo)
                    editingId = null
                }
                title = ""
                deadline = ""
            },
            modifier = Modifier.padding(top = 8.dp, start = 280.dp)
        ) {
            Text(if (editingId == null) "新增" else "儲存")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "待辦清單",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp
        )

        if (todos.isEmpty()) {
            Text("尚無待辦事項，請新增一筆。", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(todos) { todo ->
                    val all = TodoEntity(todo.id,todo.title,todo.startDate,todo.endDate)
                    TodoItemRow(
                        todo = todo,
                        onEdit = {
                            title = it.title
                            startDate = it.startDate
                            endDate = it.endDate
                            editingId = it.id
                        },
                        onDelete = { viewModel.deleteTodo(all) }
                    )
                }
            }
        }
    }
}