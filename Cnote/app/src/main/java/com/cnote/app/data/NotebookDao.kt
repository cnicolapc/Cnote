package com.cnote.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NotebookDao {

    @Query("SELECT * FROM notebooks ORDER BY name ASC")
    fun getAllNotebooks(): Flow<List<NotebookEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(notebook: NotebookEntity): Long

    @Delete
    suspend fun delete(notebook: NotebookEntity)
}
