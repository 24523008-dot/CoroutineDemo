package com.example.coroutinedemo.ui.main

import com.example.coroutinedemo.data.Task

data class TasksUiState(
    val isLoading: Boolean = false,
    val tasks: List<Task> = emptyList(),
    val announcement: String = "",
    val courseCount: Int = 0, // Latihan 1
    val errorMessage: String? = null,
    val tapCount: Int = 0,
    // Pengembangan tambahan:
    val searchQuery: String = "",
    val isSortedAscending: Boolean = false,
    val loadDurationMs: Long? = null
) {
    // Computed property untuk memfilter daftar tugas berdasarkan pencarian
    val filteredTasks: List<Task>
        get() = if (searchQuery.isBlank()) {
            tasks
        } else {
            tasks.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.course.contains(searchQuery, ignoreCase = true)
            }
        }
}