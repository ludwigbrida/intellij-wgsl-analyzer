package dev.wgsl.analyzer.editor

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiFile

class WgslEnterHandler : EnterHandlerDelegate {
    override fun preprocessEnter(
        file: PsiFile,
        editor: Editor,
        caretOffset: Ref<Int>,
        caretAdvance: Ref<Int>,
        dataContext: DataContext,
        originalHandler: EditorActionHandler?,
    ): EnterHandlerDelegate.Result {
        if (!isWeslCompatibleFile(editor)) return EnterHandlerDelegate.Result.Continue

        val document = editor.document
        val offset = caretOffset.get()
        val line = document.getLineNumber(offset)
        val lineStart = document.getLineStartOffset(line)
        val linePrefix = document.charsSequence.subSequence(lineStart, offset).toString()
        if (linePrefix.trimEnd() !in openingBlockComments) return EnterHandlerDelegate.Result.Continue

        val indent = linePrefix.takeWhile { it == ' ' || it == '\t' }
        val inserted = "\n$indent * \n$indent */"
        document.insertString(offset, inserted)
        editor.caretModel.moveToOffset(offset + 1 + indent.length + " * ".length)
        return EnterHandlerDelegate.Result.Stop
    }

    override fun postProcessEnter(file: PsiFile, editor: Editor, dataContext: DataContext): EnterHandlerDelegate.Result {
        if (!isWeslCompatibleFile(editor)) return EnterHandlerDelegate.Result.Continue

        val document = editor.document
        val caretOffset = editor.caretModel.offset
        val line = document.getLineNumber(caretOffset)
        if (line == 0) return EnterHandlerDelegate.Result.Continue

        val previousLineStart = document.getLineStartOffset(line - 1)
        val previousLineEnd = document.getLineEndOffset(line - 1)
        val previousLine = document.charsSequence.subSequence(previousLineStart, previousLineEnd).toString()
        val currentLineStart = document.getLineStartOffset(line)
        val currentIndent = document.charsSequence
            .subSequence(currentLineStart, caretOffset)
            .takeWhile { it == ' ' || it == '\t' }
            .toString()

        if (previousLine.trimEnd().trimStart() in openingBlockComments) {
            val inserted = " * \n$currentIndent */"
            document.insertString(caretOffset, inserted)
            editor.caretModel.moveToOffset(caretOffset + " * ".length)
            return EnterHandlerDelegate.Result.Stop
        }

        val continuation = lineCommentPrefix(previousLine)
            ?: blockCommentPrefix(previousLine, document.charsSequence, currentLineStart)
            ?: return EnterHandlerDelegate.Result.Continue

        document.insertString(caretOffset, continuation)
        editor.caretModel.moveToOffset(caretOffset + continuation.length)
        return EnterHandlerDelegate.Result.Stop
    }

    private fun lineCommentPrefix(line: String): String? {
        val marker = line.trimStart().let {
            when {
                it.startsWith("///") -> "///"
                it.startsWith("//!") -> "//!"
                it.startsWith("//") -> "//"
                else -> return null
            }
        }
        return "$marker "
    }

    private fun blockCommentPrefix(previousLine: String, text: CharSequence, offset: Int): String? {
        if (!isInsideBlockComment(text, offset)) return null
        val content = previousLine.trimStart()
        return if (content.startsWith("/*") || content.startsWith("*")) "* " else null
    }

    private fun isInsideBlockComment(text: CharSequence, endOffset: Int): Boolean {
        var depth = 0
        var index = 0
        var inLineComment = false
        while (index < endOffset) {
            if (inLineComment) {
                if (text[index] == '\n' || text[index] == '\r') inLineComment = false
                index++
            } else if (index + 1 < endOffset && text[index] == '/' && text[index + 1] == '/') {
                inLineComment = true
                index += 2
            } else if (index + 1 < endOffset && text[index] == '/' && text[index + 1] == '*') {
                depth++
                index += 2
            } else if (index + 1 < endOffset && text[index] == '*' && text[index + 1] == '/') {
                depth = (depth - 1).coerceAtLeast(0)
                index += 2
            } else {
                index++
            }
        }
        return depth > 0
    }

    private fun isWeslCompatibleFile(editor: Editor): Boolean =
        FileDocumentManager.getInstance().getFile(editor.document)?.extension.let {
            it.equals("wgsl", ignoreCase = true) || it.equals("wesl", ignoreCase = true)
        }

    private companion object {
        val openingBlockComments = setOf("/*", "/**")
    }
}
