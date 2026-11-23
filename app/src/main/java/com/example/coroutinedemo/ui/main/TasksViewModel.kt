package com.example.coroutinedemo.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coroutinedemo.data.TaskRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.system.measureTimeMillis

class TasksViewModel(
    private val repository: TaskRepository = TaskRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    fun loadData(shouldFail: Boolean = false) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    loadDurationMs = null
                )
            }

            try {
                var loadedTasks = emptyList<com.example.coroutinedemo.data.Task>()
                var loadedAnnouncement = ""
                var loadedCourseCount = 0

                // Mengukur total waktu eksekusi konkuren
                val duration = measureTimeMillis {
                    coroutineScope {
                        val tasksDeferred = async { repository.getTasks(shouldFail) }
                        val announcementDeferred = async { repository.getAnnouncement() }
                        // Latihan 1: async ketiga untuk getCourseCount()
                        val courseCountDeferred = async { repository.getCourseCount() }

                        loadedTasks = tasksDeferred.await()
                        loadedAnnouncement = announcementDeferred.await()
                        loadedCourseCount = courseCountDeferred.await()
                    }
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        tasks = loadedTasks,
                        announcement = loadedAnnouncement,
                        courseCount = loadedCourseCount,
                        isSortedAscending = false,
                        loadDurationMs = duration
                    )
                }
            } catch (e: CancellationException) {
                // Latihan 3: Wajib lempar ulang CancellationException agar pembatalan tidak dianggap error
                throw e
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load: ${e.message}"
                    )
                }
            }
        }
    }

    fun cancelLoading() {
        loadJob?.cancel()
        _uiState.update {
            it.copy(
                isLoading = false,
                errorMessage = "Loading cancelled"
            )
        }
    }

    // Latihan 6: Mengurutkan daftar tugas berdasarkan judul di Dispatchers.Default
    fun sortTasksByTitle() {
        val currentTasks = _uiState.value.tasks
        if (currentTasks.isEmpty()) return

        viewModelScope.launch {
            val nextAscending = !_uiState.value.isSortedAscending
            val sortedList = withContext(Dispatchers.Default) {
                if (nextAscending) {
                    currentTasks.sortedBy { it.title.lowercase() }
                } else {
                    currentTasks.sortedByDescending { it.title.lowercase() }
                }
            }
            _uiState.update {
                it.copy(
                    tasks = sortedList,
                    isSortedAscending = nextAscending
                )
            }
        }
    }

    // Pengembangan: Mengubah status selesai tugas
    fun toggleTaskCompleted(taskId: Int) {
        _uiState.update { currentState ->
            val updatedTasks = currentState.tasks.map { task ->
                if (task.id == taskId) task.copy(isCompleted = !task.isCompleted) else task
            }
            currentState.copy(tasks = updatedTasks)
        }
    }

    // Pengembangan: Memperbarui kata kunci pencarian
    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onTap() {
        _uiState.update { it.copy(tapCount = it.tapCount + 1) }
    }

    fun blockMainThread() {
        Thread.sleep(3000L)
    }
}