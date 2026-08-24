package dev.wgsl.analyzer.highlighting

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType

internal class WgslSyntaxHighlighter : SyntaxHighlighterBase() {
    override fun getHighlightingLexer(): Lexer = WgslLexer()

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> = when (tokenType) {
        WgslTokenTypes.LINE_COMMENT -> pack(DefaultLanguageHighlighterColors.LINE_COMMENT)
        WgslTokenTypes.BLOCK_COMMENT -> pack(DefaultLanguageHighlighterColors.BLOCK_COMMENT)
        WgslTokenTypes.DOC_COMMENT -> pack(DefaultLanguageHighlighterColors.DOC_COMMENT)
        WgslTokenTypes.ATTRIBUTE -> pack(DefaultLanguageHighlighterColors.METADATA)
        WgslTokenTypes.KEYWORD -> pack(DefaultLanguageHighlighterColors.KEYWORD)
        WgslTokenTypes.BUILTIN_TYPE -> pack(DefaultLanguageHighlighterColors.KEYWORD)
        WgslTokenTypes.TYPE_NAME -> pack(DefaultLanguageHighlighterColors.CLASS_REFERENCE)
        WgslTokenTypes.FUNCTION_DECLARATION,
        WgslTokenTypes.FUNCTION_CALL -> pack(DefaultLanguageHighlighterColors.FUNCTION_DECLARATION)
        WgslTokenTypes.FIELD -> pack(DefaultLanguageHighlighterColors.INSTANCE_FIELD)
        WgslTokenTypes.NUMBER -> pack(DefaultLanguageHighlighterColors.NUMBER)
        WgslTokenTypes.BOOLEAN -> pack(DefaultLanguageHighlighterColors.KEYWORD)
        WgslTokenTypes.OPERATOR -> pack(DefaultLanguageHighlighterColors.OPERATION_SIGN)
        WgslTokenTypes.BRACES -> pack(DefaultLanguageHighlighterColors.BRACES)
        WgslTokenTypes.BRACKETS -> pack(DefaultLanguageHighlighterColors.BRACKETS)
        WgslTokenTypes.PARENTHESES -> pack(DefaultLanguageHighlighterColors.PARENTHESES)
        WgslTokenTypes.PUNCTUATION -> pack(DefaultLanguageHighlighterColors.COMMA)
        else -> emptyArray()
    }
}
