package com.cnote.app.util

import android.content.Context
import org.jsoup.Jsoup
import java.io.File
import java.net.URL
import java.util.UUID
import kotlin.text.Regex

data class WebClipResult(
    val title: String,
    val textContent: String,
    val links: List<Pair<String, String>>, // testo del link -> url
    val imagePaths: List<String>,
    val htmlPath: String?
)

/**
 * Scarica ed elabora una pagina web. Va sempre chiamata da un thread in background
 * (es. Dispatchers.IO), perché esegue richieste di rete.
 *
 * @param saveOffline se true, scarica anche una copia HTML completa (con le immagini
 *   riscritte su percorsi locali) così la nota resta consultabile "come nel browser"
 *   anche se in futuro la pagina originale sparisse dalla fonte.
 */
fun clipWebPage(context: Context, url: String, saveOffline: Boolean): WebClipResult? {
    return try {
        val doc = Jsoup.connect(url)
            .userAgent("Mozilla/5.0 (Android) CnoteApp")
            .timeout(15000)
            .get()

        val title = doc.title().ifBlank { url }
        val textContent = doc.body()?.text()?.take(6000) ?: ""

        val links = doc.select("a[href]")
            .mapNotNull { el ->
                val href = el.absUrl("href")
                val text = el.text().trim()
                if (href.isNotBlank() && text.isNotBlank()) text to href else null
            }
            .distinct()
            .take(30)

        var imagePaths = emptyList<String>()
        var htmlPath: String? = null

        if (saveOffline) {
            val clipDir = File(context.filesDir, "webclips").apply { mkdirs() }
            val clipId = UUID.randomUUID().toString()

            val downloadedImages = mutableListOf<String>()
            val images = doc.select("img[src]").take(8)
            for (img in images) {
                val src = img.absUrl("src")
                if (src.isBlank()) continue
                try {
                    val bytes = URL(src).openStream().use { it.readBytes() }
                    val imgFile = File(clipDir, "${clipId}_${downloadedImages.size}.jpg")
                    imgFile.writeBytes(bytes)
                    // riscriviamo il src nell'HTML salvato così punta al file locale
                    img.attr("src", imgFile.toURI().toString())
                    downloadedImages.add(imgFile.absolutePath)
                } catch (e: Exception) {
                    // se un'immagine fallisce, si continua con le altre
                }
            }
            imagePaths = downloadedImages

            val htmlFile = File(clipDir, "$clipId.html")
            htmlFile.writeText(doc.outerHtml())
            htmlPath = htmlFile.absolutePath
        }

        WebClipResult(
            title = title,
            textContent = textContent,
            links = links,
            imagePaths = imagePaths,
            htmlPath = htmlPath
        )
    } catch (e: Exception) {
        null
    }
}

/** Trova il primo URL http/https dentro un testo condiviso (i browser a volte aggiungono testo prima del link). */
fun extractFirstUrl(text: String): String? {
    val regex = Regex("https?://\\S+")
    return regex.find(text)?.value
}
