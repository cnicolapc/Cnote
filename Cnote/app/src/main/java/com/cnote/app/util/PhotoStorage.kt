package com.cnote.app.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

object PhotoStorage {

    private fun imagesDir(context: Context): File {
        val dir = File(context.filesDir, "images")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /**
     * Crea un nuovo file vuoto pronto a ricevere lo scatto della fotocamera,
     * e restituisce sia il path assoluto (da salvare nella nota) sia la Uri
     * "content://" necessaria per passare il file alla fotocamera di sistema.
     */
    fun createCameraTarget(context: Context): Pair<String, Uri> {
        val file = File(imagesDir(context), "IMG_${UUID.randomUUID()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return file.absolutePath to uri
    }

    /**
     * Copia un'immagine scelta dalla galleria (Uri esterna, di validità incerta nel tempo)
     * dentro la memoria interna dell'app, così la nota continua a vederla anche in futuro.
     */
    fun copyFromGallery(context: Context, sourceUri: Uri): String? {
        return try {
            val destFile = File(imagesDir(context), "IMG_${UUID.randomUUID()}.jpg")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }
}
