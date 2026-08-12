package com.cnote.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.cnote.app.data.AppDatabase
import com.cnote.app.data.NoteEntity
import com.cnote.app.data.NoteType
import com.cnote.app.ui.theme.CnoteTheme
import com.cnote.app.util.clipWebPage
import com.cnote.app.util.extractFirstUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ShareReceiverActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedText = when {
            intent?.action == Intent.ACTION_SEND && intent.type == "text/plain" ->
                intent.getStringExtra(Intent.EXTRA_TEXT)
            else -> null
        }
        val url = sharedText?.let { extractFirstUrl(it) }

        setContent {
            CnoteTheme {
                if (url == null) {
                    InvalidShareDialog(onDismiss = { finish() })
                } else {
                    WebClipChoiceDialog(
                        url = url,
                        onDismiss = { finish() },
                        onChoice = { saveOffline -> saveClipAndOpen(url, saveOffline) }
                    )
                }
            }
        }
    }

    private fun saveClipAndOpen(url: String, saveOffline: Boolean) {
        lifecycleScope.launch {
            val db = AppDatabase.getInstance(applicationContext)
            val result = withContext(Dispatchers.IO) {
                clipWebPage(applicationContext, url, saveOffline)
            }

            val note = if (result != null) {
                NoteEntity(
                    title = result.title,
                    content = buildString {
                        append(result.textContent)
                        if (result.links.isNotEmpty()) {
                            append("\n\n— Link nella pagina —\n")
                            result.links.forEach { (text, href) ->
                                append("• $text: $href\n")
                            }
                        }
                    },
                    photoUris = result.imagePaths,
                    type = NoteType.TEXT,
                    sourceUrl = url,
                    webClipHtmlPath = result.htmlPath
                )
            } else {
                NoteEntity(
                    title = url,
                    content = "Impossibile scaricare il contenuto della pagina. Link originale:\n$url",
                    type = NoteType.TEXT,
                    sourceUrl = url
                )
            }

            val newId = db.noteDao().upsert(note)

            val openIntent = Intent(this@ShareReceiverActivity, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("open_note_id", newId)
            }
            startActivity(openIntent)
            finish()
        }
    }
}

@Composable
private fun WebClipChoiceDialog(
    url: String,
    onDismiss: () -> Unit,
    onChoice: (saveOffline: Boolean) -> Unit
) {
    var isSaving by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text("Salva in Cnote") },
        text = {
            if (isSaving) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Ritaglio della pagina in corso...")
                }
            } else {
                Column {
                    Text(
                        text = url,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Come vuoi salvarla?")
                }
            }
        },
        confirmButton = {
            if (!isSaving) {
                TextButton(onClick = { isSaving = true; onChoice(true) }) {
                    Text("Pagina completa (offline)")
                }
            }
        },
        dismissButton = {
            if (!isSaving) {
                TextButton(onClick = { isSaving = true; onChoice(false) }) {
                    Text("Solo link")
                }
            }
        }
    )
}

@Composable
private fun InvalidShareDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nessun link trovato") },
        text = { Text("Il contenuto condiviso non sembra contenere un link a una pagina web.") },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK") }
        }
    )
}
