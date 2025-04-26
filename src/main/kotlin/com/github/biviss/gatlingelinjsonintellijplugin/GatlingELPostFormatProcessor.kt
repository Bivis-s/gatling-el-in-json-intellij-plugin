package com.github.biviss.gatlingelinjsonintellijplugin

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.impl.source.codeStyle.PostFormatProcessor

class GatlingELPostFormatProcessor : PostFormatProcessor {

    override fun processElement(element: PsiElement, settings: CodeStyleSettings) = element

    override fun processText(
        file: PsiFile, range: TextRange, settings: CodeStyleSettings
    ): TextRange {
        val project = file.project
        val document = PsiDocumentManager.getInstance(project).getDocument(file) ?: return range
        val markers = document.getUserData(GatlingKeys.EL_MARKERS_KEY) ?: return range
        if (markers.isEmpty()) return range

        WriteCommandAction.runWriteCommandAction(project) {
            for (mk in markers.asReversed()) {
                val start = mk.startOffset
                val end = mk.endOffset
                if (end < document.textLength && document.charsSequence[end] == '"')
                    document.deleteString(end, end + 1)
                if (start > 0 && document.charsSequence[start - 1] == '"')
                    document.deleteString(start - 1, start)
            }
            document.putUserData(GatlingKeys.EL_MARKERS_KEY, null)
        }
        return TextRange(0, document.textLength)
    }
}