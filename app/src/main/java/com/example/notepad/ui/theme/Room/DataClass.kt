package com.example.notepad.ui.theme.Room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todo_table")
data class TodoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val startDate: String,   // yyyy-MM-dd
    val endDate: String      // yyyy-MM-dd
   // val location: String // 或用 lat/lng 拆開
)

/*
@Entity(tableName = "todoClass")
data class Todo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val isDone: Boolean
)
*/