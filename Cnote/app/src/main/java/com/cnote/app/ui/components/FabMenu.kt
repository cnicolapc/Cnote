package com.cnote.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FabMenu(
    onNewText: () -> Unit,
    onNewChecklist: () -> Unit,
    onTakePhoto: () -> Unit,
    onPickFromGallery: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showPhotoChooser by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.End) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
        ) {
            Column(horizontalAlignment = Alignment.End) {
                MiniFabOption(
                    icon = Icons.Filled.CameraAlt,
                    label = "Foto",
                    onClick = { expanded = false; showPhotoChooser = true }
                )
                Spacer(modifier = Modifier.height(10.dp))
                MiniFabOption(
                    icon = Icons.Filled.CheckBox,
                    label = "Checklist",
                    onClick = { expanded = false; onNewChecklist() }
                )
                Spacer(modifier = Modifier.height(10.dp))
                MiniFabOption(
                    icon = Icons.Filled.Edit,
                    label = "Nota di testo",
                    onClick = { expanded = false; onNewText() }
                )
                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        FloatingActionButton(onClick = { expanded = !expanded }) {
            Icon(
                imageVector = if (expanded) Icons.Filled.Close else Icons.Filled.Add,
                contentDescription = "Nuova nota"
            )
        }
    }

    if (showPhotoChooser) {
        PhotoSourceDialog(
            onDismiss = { showPhotoChooser = false },
            onCamera = { showPhotoChooser = false; onTakePhoto() },
            onGallery = { showPhotoChooser = false; onPickFromGallery() }
        )
    }
}

@Composable
private fun PhotoSourceDialog(
    onDismiss: () -> Unit,
    onCamera: () -> Unit,
    onGallery: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Aggiungi foto") },
        text = {
            Column {
                TextButton(onClick = onCamera, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Scatta con la fotocamera", modifier = Modifier.weight(1f))
                }
                TextButton(onClick = onGallery, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Scegli dalla galleria", modifier = Modifier.weight(1f))
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annulla") }
        }
    )
}

@Composable
private fun MiniFabOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            tonalElevation = 2.dp,
            modifier = Modifier.padding(end = 8.dp)
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }
        SmallFloatingActionButton(onClick = onClick) {
            Icon(imageVector = icon, contentDescription = label)
        }
    }
}
