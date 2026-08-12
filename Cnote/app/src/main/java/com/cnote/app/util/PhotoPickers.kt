package com.cnote.app.util

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

data class PhotoLaunchers(
    val takePhoto: () -> Unit,
    val pickFromGallery: () -> Unit
)

/**
 * Fornisce due azioni pronte all'uso: "scatta foto" (con richiesta permesso fotocamera
 * se serve) e "scegli dalla galleria" (nessun permesso necessario, usa il selettore
 * di sistema). Il risultato (uno o più path assoluti su memoria interna) arriva
 * tramite [onPhotosReady].
 */
@Composable
fun rememberPhotoLaunchers(onPhotosReady: (List<String>) -> Unit): PhotoLaunchers {
    val context = LocalContext.current

    var pendingCameraPath by remember { mutableStateOf<String?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingCameraPath?.let { onPhotosReady(listOf(it)) }
        }
        pendingCameraPath = null
        pendingCameraUri = null
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val (path, uri) = PhotoStorage.createCameraTarget(context)
            pendingCameraPath = path
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            val paths = uris.mapNotNull { PhotoStorage.copyFromGallery(context, it) }
            if (paths.isNotEmpty()) onPhotosReady(paths)
        }
    }

    return PhotoLaunchers(
        takePhoto = {
            val hasPermission = ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                val (path, uri) = PhotoStorage.createCameraTarget(context)
                pendingCameraPath = path
                pendingCameraUri = uri
                cameraLauncher.launch(uri)
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        },
        pickFromGallery = {
            galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    )
}
