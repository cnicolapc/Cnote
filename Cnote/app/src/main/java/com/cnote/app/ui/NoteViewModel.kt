package com.cnote.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cnote.app.data.AppDatabase
import com.cnote.app.data.NoteEntity
import com.cnote.app.data.NoteRepository
import com.cnote.app.data.NotebookEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NoteRepository

    init {
        val db = AppDatabase.getInstance(application)
        repository = NoteRepository(db.noteDao(), db.notebookDao())
    }

    // null = "Tutte le note"
    private val _selectedNotebookId = MutableStateFlow<Long?>(null)
    val selectedNotebookId: StateFlow<Long?> = _selectedNotebookId

    val notebooks: StateFlow<List<NotebookEntity>> = repository.allNotebooks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val allNotes: StateFlow<List<NoteEntity>> = repository.allNotes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val notes: StateFlow<List<NoteEntity>> = combine(allNotes, _selectedNotebookId) { notes, notebookId ->
        if (notebookId == null) notes else notes.filter { it.notebookId == notebookId }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun selectNotebook(notebookId: Long?) {
        _selectedNotebookId.value = notebookId
    }

    fun save(note: NoteEntity, onSaved: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.save(note)
            onSaved(id)
        }
    }

    fun delete(note: NoteEntity) {
        viewModelScope.launch {
            repository.delete(note)
        }
    }

    suspend fun getNoteById(id: Long): NoteEntity? = repository.getNoteById(id)

    fun addNotebook(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            repository.saveNotebook(NotebookEntity(name = trimmed))
        }
    }

    fun deleteNotebook(notebook: NotebookEntity) {
        viewModelScope.launch {
            if (_selectedNotebookId.value == notebook.id) {
                _selectedNotebookId.value = null
            }
            repository.deleteNotebook(notebook)
        }
    }
}
