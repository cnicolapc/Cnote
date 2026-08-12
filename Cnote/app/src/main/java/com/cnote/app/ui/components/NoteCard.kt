package com.cnote.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cnote.app.data.NoteEntity
import com.cnote.app.data.NoteType
import com.cnote.app.util.RichText

@Composable
fun NoteCard(
    note: NoteEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = try {
        Color(android.graphics.Color.parseColor(note.colorHex))
    } catch (e: Exception) {
        Color.White
    }

    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (note.isPinned) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Fissata",
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.End)
                )
            }
            if (note.title.isNotBlank()) {
                Text(
                    text = RichText.toAnnotatedString(note.title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            if (note.photoUris.isNotEmpty()) {
                PhotoThumbnail(
                    path = note.photoUris.first(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
                if (note.photoUris.size > 1) {
                    Text(
                        text = "+${note.photoUris.size - 1} altre foto",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            when (note.type) {
                NoteType.TEXT -> {
                    if (note.content.isNotBlank()) {
                        Text(
                            text = RichText.toAnnotatedString(note.content),
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 8,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                NoteType.CHECKLIST -> {
                    val itemsToShow = note.checklistItems.take(6)
                    Column {
                        itemsToShow.forEach { item ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = item.checked,
                                    onCheckedChange = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = item.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        val remaining = note.checklistItems.size - itemsToShow.size
                        if (remaining > 0) {
                            Text(
                                text = "+$remaining altri",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}
