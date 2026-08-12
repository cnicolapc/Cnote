package com.cnote.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cnote.app.data.NotebookEntity

@Composable
fun NotebookDrawerContent(
    notebooks: List<NotebookEntity>,
    selectedNotebookId: Long?,
    onSelectNotebook: (Long?) -> Unit,
    onAddNotebook: (String) -> Unit,
    onDeleteNotebook: (NotebookEntity) -> Unit
) {
    var showNewNotebookDialog by remember { mutableStateOf(false) }

    ModalDrawerSheet {
        Column(modifier = Modifier.padding(vertical = 12.dp)) {
            Text(
                text = "Cnote",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            )

            NavigationDrawerItem(
                label = { Text("Tutte le note") },
                icon = { Icon(Icons.Filled.StickyNote2, contentDescription = null) },
                selected = selectedNotebookId == null,
                onClick = { onSelectNotebook(null) },
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Text(
                text = "Taccuini",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            notebooks.forEach { notebook ->
                NavigationDrawerItem(
                    label = { Text(notebook.name) },
                    icon = { Icon(Icons.Filled.Book, contentDescription = null) },
                    selected = selectedNotebookId == notebook.id,
                    onClick = { onSelectNotebook(notebook.id) },
                    badge = {
                        IconButton(onClick = { onDeleteNotebook(notebook) }) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Elimina taccuino \"${notebook.name}\"",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }

            NavigationDrawerItem(
                label = { Text("Nuovo taccuino") },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                selected = false,
                onClick = { showNewNotebookDialog = true },
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }

    if (showNewNotebookDialog) {
        NewNotebookDialog(
            onConfirm = { name ->
                onAddNotebook(name)
                showNewNotebookDialog = false
            },
            onDismiss = { showNewNotebookDialog = false }
        )
    }
}

@Composable
private fun NewNotebookDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuovo taccuino") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Nome del taccuino") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(text) },
                enabled = text.isNotBlank()
            ) {
                Text("Crea")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}
