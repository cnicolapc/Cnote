package com.cnote.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters

enum class NoteType {
    TEXT,
    CHECKLIST
}

/**
 * Un singolo elemento di una checklist.
 * Serializzato come stringa dentro la nota (vedi Converters) per semplicità:
 * "fatto|testo" separati da "\n" tra un elemento e l'altro.
 */
data class ChecklistItem(
    val text: String,
    val checked: Boolean = false
)

class Converters {
    @TypeConverter
    fun fromChecklist(items: List<ChecklistItem>): String {
        return items.joinToString("\n") { item ->
            val safeText = item.text.replace("\n", " ")
            "${if (item.checked) "1" else "0"}|$safeText"
        }
    }

    @TypeConverter
    fun toChecklist(data: String): List<ChecklistItem> {
        if (data.isBlank()) return emptyList()
        return data.split("\n").mapNotNull { line ->
            val parts = line.split("|", limit = 2)
            if (parts.size == 2) {
                ChecklistItem(text = parts[1], checked = parts[0] == "1")
            } else null
        }
    }

    @TypeConverter
    fun fromPhotoUris(paths: List<String>): String = paths.joinToString("\n")

    @TypeConverter
    fun toPhotoUris(data: String): List<String> =
        if (data.isBlank()) emptyList() else data.split("\n")
}

@Entity(tableName = "notes")
@TypeConverters(Converters::class)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String = "",
    val content: String = "",                 // usato se type == TEXT
    val checklistItems: List<ChecklistItem> = emptyList(), // usato se type == CHECKLIST
    val photoUris: List<String> = emptyList(),  // path assoluti delle foto allegate alla nota
    val type: NoteType = NoteType.TEXT,
    val colorHex: String = "#FFFFFF",
    val notebookId: Long? = null,              // predisposto per lo step "Taccuini"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val sourceUrl: String? = null,          // URL originale se la nota viene dal web clipper
    val webClipHtmlPath: String? = null     // percorso locale della pagina salvata offline (se presente)
)
