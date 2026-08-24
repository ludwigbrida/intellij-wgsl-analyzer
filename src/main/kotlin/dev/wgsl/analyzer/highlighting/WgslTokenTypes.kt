package dev.wgsl.analyzer.highlighting

import com.intellij.psi.tree.IElementType
import com.intellij.psi.TokenType
import dev.wgsl.analyzer.WgslLanguage

internal object WgslTokenTypes {
    val WHITE_SPACE = TokenType.WHITE_SPACE
    val LINE_COMMENT = token("LINE_COMMENT")
    val BLOCK_COMMENT = token("BLOCK_COMMENT")
    val DOC_COMMENT = token("DOC_COMMENT")
    val ATTRIBUTE = token("ATTRIBUTE")
    val KEYWORD = token("KEYWORD")
    val BUILTIN_TYPE = token("BUILTIN_TYPE")
    val TYPE_NAME = token("TYPE_NAME")
    val IDENTIFIER = token("IDENTIFIER")
    val FUNCTION_DECLARATION = token("FUNCTION_DECLARATION")
    val FUNCTION_CALL = token("FUNCTION_CALL")
    val FIELD = token("FIELD")
    val NUMBER = token("NUMBER")
    val BOOLEAN = token("BOOLEAN")
    val OPERATOR = token("OPERATOR")
    val BRACES = token("BRACES")
    val BRACKETS = token("BRACKETS")
    val PARENTHESES = token("PARENTHESES")
    val PUNCTUATION = token("PUNCTUATION")
    val BAD_CHARACTER = TokenType.BAD_CHARACTER

    private fun token(debugName: String) = IElementType("WGSL_$debugName", WgslLanguage)
}
