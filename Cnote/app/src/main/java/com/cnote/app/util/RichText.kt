package com.cnote.app.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue

/**
 * Formattazione testo semplificata (non annidabile: non si può avere grassetto+colore
 * sulla stessa porzione contemporaneamente, solo l'una o l'altra), basata su marcatori
 * testuali leggeri, così il contenuto resta salvabile come semplice stringa in Room:
 *
 *   **testo**          -> grassetto
 *   __testo__          -> corsivo
 *   [c#RRGGBB]testo[/c] -> colorato
 */
object RichText {

    private val boldRegex = Regex("\\*\\*(.+?)\\*\\*", RegexOption.DOT_MATCHES_ALL)
    private val italicRegex = Regex("__(.+?)__", RegexOption.DOT_MATCHES_ALL)
    private val colorRegex = Regex("\\[c#([0-9A-Fa-f]{6})](.+?)\\[/c]", RegexOption.DOT_MATCHES_ALL)

    /** Avvolge la selezione corrente del campo con i marcatori indicati (o li inserisce vuoti se non c'è selezione). */
    fun wrapSelection(value: TextFieldValue, prefix: String, suffix: String = prefix): TextFieldValue {
        val start = value.selection.min
        val end = value.selection.max
        val newText = value.text.substring(0, start) + prefix + value.text.substring(start, end) + suffix + value.text.substring(end)
        val newCursor = if (start == end) start + prefix.length else end + prefix.length + suffix.length
        return value.copy(text = newText, selection = androidx.compose.ui.text.TextRange(newCursor))
    }

    fun wrapSelectionWithColor(value: TextFieldValue, hex: String): TextFieldValue =
        wrapSelection(value, "[c#${hex.removePrefix("#")}]", "[/c]")

    /** Converte il markup in un AnnotatedString pronto per la visualizzazione (grassetto/corsivo/colore veri). */
    fun toAnnotatedString(raw: String): AnnotatedString {
        data class Token(val start: Int, val end: Int, val innerText: String, val style: SpanStyle)

        val tokens = mutableListOf<Token>()

        boldRegex.findAll(raw).forEach { m ->
            tokens.add(Token(m.range.first, m.range.last + 1, m.groupValues[1], SpanStyle(fontWeight = FontWeight.Bold)))
        }
        italicRegex.findAll(raw).forEach { m ->
            tokens.add(Token(m.range.first, m.range.last + 1, m.groupValues[1], SpanStyle(fontStyle = FontStyle.Italic)))
        }
        colorRegex.findAll(raw).forEach { m ->
            val hex = "#" + m.groupValues[1]
            val color = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.Unspecified }
            tokens.add(Token(m.range.first, m.range.last + 1, m.groupValues[2], SpanStyle(color = color)))
        }

        // Ordiniamo i token per posizione; essendo un formato non annidabile,
        // eventuali sovrapposizioni vengono semplicemente ignorate (si tiene la prima trovata).
        tokens.sortBy { it.start }
        val resolved = mutableListOf<Token>()
        var lastEnd = -1
        for (t in tokens) {
            if (t.start >= lastEnd) {
                resolved.add(t)
                lastEnd = t.end
            }
        }

        return buildAnnotatedString {
            var cursor = 0
            for (t in resolved) {
                if (t.start > cursor) {
                    append(raw.substring(cursor, t.start))
                }
                withStyle(t.style) {
                    append(t.innerText)
                }
                cursor = t.end
            }
            if (cursor < raw.length) {
                append(raw.substring(cursor))
            }
        }
    }
}
