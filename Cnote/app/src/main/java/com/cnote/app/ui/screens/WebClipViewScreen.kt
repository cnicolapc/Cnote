package com.cnote.app.ui.screens

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.cnote.app.ui.NoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebClipViewScreen(
    viewModel: NoteViewModel,
    noteId: Long,
    onBack: () -> Unit
) {
    var htmlPath by remember { mutableStateOf<String?>(null) }
    var title by remember { mutableStateOf("Pagina salvata") }

    LaunchedEffect(noteId) {
        val note = viewModel.getNoteById(noteId)
        htmlPath = note?.webClipHtmlPath
        if (!note?.title.isNullOrBlank()) title = note!!.title
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { padding ->
        val path = htmlPath
        if (path != null) {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = false
                        settings.allowFileAccess = true
                        settings.allowUniversalAccessFromFileURLs = true
                        loadUrl("file://$path")
                    }
                }
            )
        } else {
            Text(
                "Nessuna copia offline disponibile per questa nota.",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        }
    }
}
