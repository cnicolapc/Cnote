package com.cnote.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cnote.app.data.ChecklistItem
import com.cnote.app.data.NoteEntity
import com.cnote.app.data.NoteType
import com.cnote.app.data.NotebookEntity
import com.cnote.app.ui.NoteViewModel
import com.cnote.app.ui.components.FullScreenPhotoViewer
import com.cnote.app.ui.components.PhotoThumbnail
import com.cnote.app.ui.theme.NoteColors
import com.cnote.app.util.RichText
import com.cnote.app.util.openImageExternally
import com.cnote.app.util.rememberPhotoLaunchers
import kotlinx.coroutines.launch

@Composable
private fun transparentFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color.Transparent,
    unfocusedBorderColor = Color.Transparent,
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent
)

/**
 * Rende maiuscola la prima lettera di ogni parola, lasciando invariato il resto
 * (così si possono comunque scrivere sigle come "PDF" senza che vengano toccate).
 */
private fun toTitleCase(input: String): String {
    if (input.isEmpty()) return input
    val sb = StringBuilder(input.length)
    var capitalizeNext = true
    for (c in input) {
        if (c.isWhitespace()) {
            capitalizeNext = true
            sb.append(c)
        } else {
            sb.append(if (capitalizeNext) c.uppercaseChar() else c)
            capitalizeNext = false
        }
    }
    return sb.toString()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditScreen(
    viewModel: NoteViewModel,
    noteId: Long?,
    initialType: NoteType,
    onBack: () -> Unit,
    onOpenWebClip: (Long) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val notebooks by viewModel.notebooks.collectAsState()

    var loadedOriginal by remember { mutableStateOf<NoteEntity?>(null) }
    var titleField by remember { mutableStateOf(TextFieldValue("")) }
    var contentField by remember { mutableStateOf(TextFieldValue("")) }
    var checklist by remember { mutableStateOf(listOf<ChecklistItem>()) }
    var colorHex by remember { mutableStateOf("#FFFFFF") }
    var isPinned by remember { mutableStateOf(false) }
    var newItemText by remember { mutableStateOf("") }
    var showColorPicker by remember { mutableStateOf(false) }
    var selectedNotebookId by remember { mutableStateOf<Long?>(null) }
    var photoUris by remember { mutableStateOf(listOf<String>()) }
    var showPhotoChooser by remember { mutableStateOf(false) }
    var fullScreenPhotoPath by remember { mutableStateOf<String?>(null) }
    var previewMode by remember { mutableStateOf(false) }

    val photoLaunchers = rememberPhotoLaunchers { newPaths ->
        photoUris = photoUris + newPaths
    }

    val type = loadedOriginal?.type ?: initialType

    LaunchedEffect(noteId) {
        if (noteId != null) {
            val existing = viewModel.getNoteById(noteId)
            if (existing != null) {
                loadedOriginal = existing
                titleField = TextFieldValue(existing.title)
                contentField = TextFieldValue(existing.content)
                checklist = existing.checklistItems
                colorHex = existing.colorHex
                isPinned = existing.isPinned
                selectedNotebookId = existing.notebookId
                photoUris = existing.photoUris
            }
        }
    }

    fun persistAndBack() {
        val hasContent = titleField.text.isNotBlank() || contentField.text.isNotBlank() ||
            checklist.isNotEmpty() || photoUris.isNotEmpty()
        if (hasContent) {
            val note = NoteEntity(
                id = loadedOriginal?.id ?: 0,
                title = titleField.text,
                content = contentField.text,
                checklistItems = checklist,
                photoUris = photoUris,
                type = type,
                colorHex = colorHex,
                notebookId = selectedNotebookId,
                createdAt = loadedOriginal?.createdAt ?: System.currentTimeMillis(),
                isPinned = isPinned,
                sourceUrl = loadedOriginal?.sourceUrl,
                webClipHtmlPath = loadedOriginal?.webClipHtmlPath
            )
            viewModel.save(note)
        } else if (loadedOriginal != null) {
            viewModel.delete(loadedOriginal!!)
        }
        onBack()
    }

    val bgColor = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        Color.White
    }

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { persistAndBack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                },
                actions = {
                    IconButton(onClick = { previewMode = !previewMode }) {
                        Icon(
                            imageVector = if (previewMode) Icons.Filled.EditNote else Icons.Filled.Visibility,
                            contentDescription = if (previewMode) "Modifica" else "Anteprima"
                        )
                    }
                    IconButton(onClick = { showPhotoChooser = true }) {
                        Icon(Icons.Filled.AddPhotoAlternate, contentDescription = "Aggiungi foto")
                    }
                    IconButton(onClick = { isPinned = !isPinned }) {
                        Icon(
                            imageVector = if (isPinned) Icons.Filled.Star else Icons.Filled.StarBorder,
                            contentDescription = "Fissa nota"
                        )
                    }
                    IconButton(onClick = { showColorPicker = !showColorPicker }) {
                        Icon(Icons.Filled.Palette, contentDescription = "Colore nota")
                    }
                    if (loadedOriginal != null) {
                        IconButton(onClick = {
                            scope.launch {
                                loadedOriginal?.let { viewModel.delete(it) }
                                onBack()
                            }
                        }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Elimina")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            if (showColorPicker) {
                ColorPickerRow(
                    selected = colorHex,
                    onSelect = { colorHex = it; showColorPicker = false }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Se la nota proviene dal web clipper, mostriamo la fonte e un tasto per vederla come pagina web
            val sourceUrl = loadedOriginal?.sourceUrl
            val webClipPath = loadedOriginal?.webClipHtmlPath
            if (sourceUrl != null) {
                Surface(
                    color = Color.Black.copy(alpha = 0.05f),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = sourceUrl,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        if (webClipPath != null && loadedOriginal?.id != null) {
                            TextButton(onClick = { onOpenWebClip(loadedOriginal!!.id) }) {
                                Text("Apri come pagina web")
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (previewMode) {
                Text(
                    text = RichText.toAnnotatedString(titleField.text),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 24.sp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            } else {
                OutlinedTextField(
                    value = titleField,
                    onValueChange = { titleField = it.copy(text = toTitleCase(it.text)) },
                    placeholder = { Text("Titolo") },
                    textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 24.sp),
                    colors = transparentFieldColors(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier.fillMaxWidth()
                )
                FormattingToolbar(
                    value = titleField,
                    onChange = { titleField = it }
                )
            }

            NotebookSelectorChip(
                notebooks = notebooks,
                selectedNotebookId = selectedNotebookId,
                onSelect = { selectedNotebookId = it }
            )

            if (photoUris.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(photoUris, key = { it }) { path ->
                        Box {
                            PhotoThumbnail(
                                path = path,
                                modifier = Modifier.size(100.dp),
                                onClick = { fullScreenPhotoPath = path }
                            )
                            IconButton(
                                onClick = { photoUris = photoUris.filterNot { it == path } },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(24.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Cancel,
                                    contentDescription = "Rimuovi foto",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (type) {
                NoteType.TEXT -> {
                    if (previewMode) {
                        Text(
                            text = RichText.toAnnotatedString(contentField.text),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                    } else {
                        Column(modifier = Modifier.weight(1f)) {
                            FormattingToolbar(
                                value = contentField,
                                onChange = { contentField = it }
                            )
                            OutlinedTextField(
                                value = contentField,
                                onValueChange = { contentField = it },
                                placeholder = { Text("Nota...") },
                                colors = transparentFieldColors(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            )
                        }
                    }
                }
                NoteType.CHECKLIST -> {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(checklist.size) { index ->
                            val item = checklist[index]
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = item.checked,
                                    onCheckedChange = { checked ->
                                        checklist = checklist.toMutableList().also {
                                            it[index] = item.copy(checked = checked)
                                        }
                                    }
                                )
                                OutlinedTextField(
                                    value = item.text,
                                    onValueChange = { newText ->
                                        checklist = checklist.toMutableList().also {
                                            it[index] = item.copy(text = newText)
                                        }
                                    },
                                    textStyle = if (item.checked) {
                                        MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.LineThrough)
                                    } else MaterialTheme.typography.bodyMedium,
                                    colors = transparentFieldColors(),
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = {
                                    checklist = checklist.toMutableList().also { it.removeAt(index) }
                                }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Rimuovi")
                                }
                            }
                        }
                        item {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.padding(start = 12.dp))
                                OutlinedTextField(
                                    value = newItemText,
                                    onValueChange = { newItemText = it },
                                    placeholder = { Text("Aggiungi elemento") },
                                    colors = transparentFieldColors(),
                                    modifier = Modifier.weight(1f),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            if (newItemText.isNotBlank()) {
                                                checklist = checklist + ChecklistItem(newItemText)
                                                newItemText = ""
                                            }
                                        }
                                    )
                                )
                                if (newItemText.isNotBlank()) {
                                    IconButton(onClick = {
                                        checklist = checklist + ChecklistItem(newItemText)
                                        newItemText = ""
                                    }) {
                                        Icon(Icons.Filled.Check, contentDescription = "Conferma")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPhotoChooser) {
        AlertDialog(
            onDismissRequest = { showPhotoChooser = false },
            title = { Text("Aggiungi foto") },
            text = {
                Column {
                    TextButton(
                        onClick = { showPhotoChooser = false; photoLaunchers.takePhoto() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Scatta con la fotocamera", modifier = Modifier.weight(1f))
                    }
                    TextButton(
                        onClick = { showPhotoChooser = false; photoLaunchers.pickFromGallery() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Scegli dalla galleria", modifier = Modifier.weight(1f))
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPhotoChooser = false }) { Text("Annulla") }
            }
        )
    }

    fullScreenPhotoPath?.let { path ->
        FullScreenPhotoViewer(
            path = path,
            onDismiss = { fullScreenPhotoPath = null },
            onOpenExternally = { openImageExternally(context, path) }
        )
    }
}

/** Barra con Grassetto / Corsivo / Colore che agisce sulla selezione corrente del campo. */
@Composable
private fun FormattingToolbar(
    value: TextFieldValue,
    onChange: (TextFieldValue) -> Unit
) {
    var showTextColorPicker by remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { onChange(RichText.wrapSelection(value, "**")) }, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Filled.FormatBold, contentDescription = "Grassetto", modifier = Modifier.size(18.dp))
        }
        IconButton(onClick = { onChange(RichText.wrapSelection(value, "__")) }, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Filled.FormatItalic, contentDescription = "Corsivo", modifier = Modifier.size(18.dp))
        }
        Box {
            IconButton(onClick = { showTextColorPicker = true }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.FormatColorText, contentDescription = "Colore testo", modifier = Modifier.size(18.dp))
            }
            DropdownMenu(expanded = showTextColorPicker, onDismissRequest = { showTextColorPicker = false }) {
                Row(modifier = Modifier.padding(8.dp)) {
                    NoteColors.drop(1).forEach { hex -> // saltiamo il bianco, poco leggibile come colore testo
                        val c = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.Black }
                        Box(
                            modifier = Modifier
                                .padding(3.dp)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(c)
                                .clickable {
                                    onChange(RichText.wrapSelectionWithColor(value, hex))
                                    showTextColorPicker = false
                                }
                        )
                    }
                }
            }
        }
        Text(
            text = "Seleziona del testo, poi formatta",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotebookSelectorChip(
    notebooks: List<NotebookEntity>,
    selectedNotebookId: Long?,
    onSelect: (Long?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = notebooks.firstOrNull { it.id == selectedNotebookId }?.name ?: "Nessun taccuino"

    Box {
        AssistChip(
            onClick = { expanded = true },
            leadingIcon = { Icon(Icons.Filled.Book, contentDescription = null, modifier = Modifier.size(16.dp)) },
            label = { Text(selectedName) }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Nessun taccuino") },
                onClick = { onSelect(null); expanded = false }
            )
            notebooks.forEach { notebook ->
                DropdownMenuItem(
                    text = { Text(notebook.name) },
                    onClick = { onSelect(notebook.id); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun ColorPickerRow(selected: String, onSelect: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth()) {
        NoteColors.forEach { hex ->
            val c = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.White }
            val isSelected = hex == selected
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(c)
                    .then(
                        if (isSelected) Modifier.background(Color.Black.copy(alpha = 0.1f))
                        else Modifier
                    )
                    .clickable { onSelect(hex) }
            )
        }
    }
}
