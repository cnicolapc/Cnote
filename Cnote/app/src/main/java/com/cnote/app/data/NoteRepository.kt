package com.cnote.app.data

import kotlinx.coroutines.flow.Flow

class NoteRepository(
    private val noteDao: NoteDao,
    private val notebookDao: NotebookDao
) {

    val allNotes: Flow<List<NoteEntity>> = noteDao.getAllNotes()
    val allNotebooks: Flow<List<NotebookEntity>> = notebookDao.getAllNotebooks()

    suspend fun getNoteById(id: Long): NoteEntity? = noteDao.getNoteById(id)

    suspend fun save(note: NoteEntity): Long {
        val toSave = note.copy(updatedAt = System.currentTimeMillis())
        return noteDao.upsert(toSave)
    }

    suspend fun delete(note: NoteEntity) = noteDao.delete(note)

    suspend fun saveNotebook(notebook: NotebookEntity): Long = notebookDao.upsert(notebook)

    suspend fun deleteNotebook(notebook: NotebookEntity) = notebookDao.delete(notebook)
}
