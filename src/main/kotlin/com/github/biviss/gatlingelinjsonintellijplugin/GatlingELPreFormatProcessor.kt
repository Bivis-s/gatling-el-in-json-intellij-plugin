package com.github.biviss.gatlingelinjsonintellijplugin

import com.intellij.json.JsonFileType
import com.intellij.lang.ASTNode
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.RangeMarker
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.impl.source.codeStyle.PreFormatProcessor

class GatlingELPreFormatProcessor : PreFormatProcessor {
    private val elRegex = Regex("#\\{[^}]+}")

    private fun isInsideJsonString(text: CharSequence, index: Int): Boolean {
        var inside = false
        var escaped = false
        for (i in 0 until index) {
            when (text[i]) {
                '\\' -> escaped = !escaped
                '"' -> if (!escaped) inside = !inside
                else -> escaped = false
            }
        }
        return inside
    }

    override fun process(element: ASTNode, range: TextRange): TextRange {
        val file = element.psi.containingFile
        if (file.fileType != JsonFileType.INSTANCE) return range

        val project = file.project
        val docManager = PsiDocumentManager.getInstance(project)
        val document = docManager.getDocument(file) ?: return range
        val text = document.charsSequence

        val first = text.indexOf("#{", range.startOffset)
        if (first < 0 || first >= range.endOffset) return range

        val markers = mutableListOf<RangeMarker>()

        WriteCommandAction.runWriteCommandAction(project) {
            for (m in elRegex.findAll(text, range.startOffset).toList().asReversed()) {
                val start = m.range.first
                val end = m.range.last + 1

                if (isInsideJsonString(text, start)) continue

                document.insertString(end, "\"")
                document.insertString(start, "\"")

                markers += document.createRangeMarker(start + 1, end + 1).apply {
                    isGreedyToLeft = true
                    isGreedyToRight = true
                }
            }
            document.putUserData(GatlingKeys.EL_MARKERS_KEY, markers)
            docManager.commitDocument(document)
        }
        return TextRange(0, document.textLength)
    }
}
