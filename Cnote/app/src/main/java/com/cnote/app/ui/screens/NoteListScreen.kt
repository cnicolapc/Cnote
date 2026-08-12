package com.cnote.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cnote.app.data.NoteEntity
import com.cnote.app.data.NoteType
import com.cnote.app.ui.NoteViewModel
import com.cnote.app.ui.components.FabMenu
import com.cnote.app.ui.components.NoteCard
import com.cnote.app.ui.components.NotebookDrawerContent
import com.cnote.app.util.rememberPhotoLaunchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    viewModel: NoteViewModel,
    onOpenNote: (Long?, NoteType) -> Unit
) {
    val notes by viewModel.notes.collectAsState()
    val notebooks by viewModel.notebooks.collectAsState()
    val selectedNotebookId by viewModel.selectedNotebookId.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val filteredNotes = remember(notes, searchQuery) {
        if (searchQuery.isBlank()) notes
        else notes.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.content.contains(searchQuery, ignoreCase = true)
        }
    }

    val selectedNotebookName = notebooks.firstOrNull { it.id == selectedNotebookId }?.name

    val photoLaunchers = rememberPhotoLaunchers { paths ->
        val newNote = NoteEntity(
            type = NoteType.TEXT,
            photoUris = paths,
            notebookId = selectedNotebookId
        )
        viewModel.save(newNote) { newId ->
            onOpenNote(newId, NoteType.TEXT)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NotebookDrawerContent(
                notebooks = notebooks,
                selectedNotebookId = selectedNotebookId,
                onSelectNotebook = { id ->
                    viewModel.selectNotebook(id)
                    scope.launch { drawerState.close() }
                },
                onAddNotebook = { name -> viewModel.addNotebook(name) },
                onDeleteNotebook = { notebook -> viewModel.deleteNotebook(notebook) }
            )
        }
    ) {
        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Cerca in Cnote") },
                                singleLine = true,
                                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                                shape = MaterialTheme.shapes.extraLarge,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Filled.Menu, contentDescription = "Menu taccuini")
                            }
                        }
                    )
                    if (selectedNotebookName != null) {
                        Text(
                            text = "Taccuino: $selectedNotebookName",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                        )
                    }
                }
            },
            floatingActionButton = {
                FabMenu(
                    onNewText = { onOpenNote(null, NoteType.TEXT) },
                    onNewChecklist = { onOpenNote(null, NoteType.CHECKLIST) },
                    onTakePhoto = { photoLaunchers.takePhoto() },
                    onPickFromGallery = { photoLaunchers.pickFromGallery() }
                )
            }
        ) { padding ->
            if (filteredNotes.isEmpty()) {
                EmptyState(modifier = Modifier.padding(padding))
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalItemSpacing = 8.dp
                ) {
                    items(filteredNotes, key = { it.id }) { note: NoteEntity ->
                        NoteCard(
                            note = note,
                            onClick = { onOpenNote(note.id, note.type) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.StickyNote2,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Le tue note appariranno qui",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}
