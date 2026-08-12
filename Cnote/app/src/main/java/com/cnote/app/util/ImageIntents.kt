package com.cnote.app.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

fun openImageExternally(context: Context, path: String) {
    val file = File(path)
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "image/*")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(intent, "Apri immagine con"))
}
