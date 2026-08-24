package dev.wgsl.analyzer.highlighting

import com.intellij.lexer.LexerBase
import com.intellij.psi.tree.IElementType

internal class WgslLexer : LexerBase() {
    private lateinit var buffer: CharSequence
    private var endOffset = 0
    private var tokenStart = 0
    private var tokenEnd = 0
    private var tokenType: IElementType? = null

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        this.buffer = buffer
        this.endOffset = endOffset
        tokenStart = startOffset
        locateToken()
    }

    override fun getState(): Int = 0

    override fun getTokenType(): IElementType? = tokenType

    override fun getTokenStart(): Int = tokenStart

    override fun getTokenEnd(): Int = tokenEnd

    override fun advance() {
        tokenStart = tokenEnd
        locateToken()
    }

    override fun getBufferSequence(): CharSequence = buffer

    override fun getBufferEnd(): Int = endOffset

    private fun locateToken() {
        if (tokenStart >= endOffset) {
            tokenType = null
            tokenEnd = endOffset
            return
        }

        val first = buffer[tokenStart]
        when {
            first.isWhitespace() -> consumeWhile { it.isWhitespace() }.also { tokenType = WgslTokenTypes.WHITE_SPACE }
            startsWith("//") -> consumeLineComment(startsWith("///") || startsWith("//!"))
            startsWith("/*") -> consumeBlockComment(startsWith("/**") && !startsWith("/**/"))
            first == '@' -> consumeAttribute()
            first.isIdentifierStart() -> consumeIdentifier()
            first.isDigit() || (first == '.' && tokenStart + 1 < endOffset && buffer[tokenStart + 1].isDigit()) -> consumeNumber()
            first in "+-*/%=!<>&|^~" -> consumeOperator()
            first == '{' || first == '}' -> single(WgslTokenTypes.BRACES)
            first == '[' || first == ']' -> single(WgslTokenTypes.BRACKETS)
            first == '(' || first == ')' -> single(WgslTokenTypes.PARENTHESES)
            first in ".,;:" -> single(WgslTokenTypes.PUNCTUATION)
            else -> single(WgslTokenTypes.BAD_CHARACTER)
        }
    }

    private fun consumeLineComment(isDocumentation: Boolean) {
        tokenEnd = tokenStart + 2
        while (tokenEnd < endOffset && buffer[tokenEnd] != '\n' && buffer[tokenEnd] != '\r') tokenEnd++
        tokenType = if (isDocumentation) WgslTokenTypes.DOC_COMMENT else WgslTokenTypes.LINE_COMMENT
    }

    private fun consumeBlockComment(isDocumentation: Boolean) {
        tokenEnd = tokenStart + 2
        var depth = 1
        while (tokenEnd < endOffset && depth > 0) {
            if (startsWith("/*", tokenEnd)) {
                depth++
                tokenEnd += 2
            } else if (startsWith("*/", tokenEnd)) {
                depth--
                tokenEnd += 2
            } else {
                tokenEnd++
            }
        }
        tokenType = if (isDocumentation) WgslTokenTypes.DOC_COMMENT else WgslTokenTypes.BLOCK_COMMENT
    }

    private fun consumeAttribute() {
        tokenEnd = tokenStart + 1
        while (tokenEnd < endOffset && buffer[tokenEnd].isWhitespace()) tokenEnd++
        if (tokenEnd < endOffset && buffer[tokenEnd].isIdentifierStart()) {
            tokenEnd++
            while (tokenEnd < endOffset && buffer[tokenEnd].isIdentifierPart()) tokenEnd++
        }
        tokenType = WgslTokenTypes.ATTRIBUTE
    }

    private fun consumeIdentifier() {
        tokenEnd = tokenStart + 1
        while (tokenEnd < endOffset && buffer[tokenEnd].isIdentifierPart()) tokenEnd++
        val word = buffer.subSequence(tokenStart, tokenEnd).toString()
        tokenType = when {
            word in booleanLiterals -> WgslTokenTypes.BOOLEAN
            word in keywords || word in weslKeywords || word in addressSpaces || word in accessModes || word in textureFormats -> WgslTokenTypes.KEYWORD
            word in builtinTypes || word.matches(vectorOrMatrixType) || word.startsWith("texture_") -> WgslTokenTypes.BUILTIN_TYPE
            followsFunctionDeclaration() -> WgslTokenTypes.FUNCTION_DECLARATION
            isLineLeadingIdentifier() && nextNonWhitespace() == ':' -> WgslTokenTypes.FIELD
            followsDot() -> WgslTokenTypes.FIELD
            nextNonWhitespace() == '(' -> WgslTokenTypes.FUNCTION_CALL
            word.first().isUpperCase() -> WgslTokenTypes.TYPE_NAME
            else -> WgslTokenTypes.IDENTIFIER
        }
    }

    private fun consumeNumber() {
        tokenEnd = tokenStart + 1
        while (tokenEnd < endOffset && buffer[tokenEnd] in numberCharacters) tokenEnd++
        tokenType = WgslTokenTypes.NUMBER
    }

    private fun consumeOperator() {
        tokenEnd = tokenStart + 1
        if (tokenEnd < endOffset && buffer[tokenEnd] in "=<>|&+-*/%^") tokenEnd++
        if (tokenEnd < endOffset && buffer[tokenEnd] == '=' && tokenEnd - tokenStart == 2) tokenEnd++
        tokenType = WgslTokenTypes.OPERATOR
    }

    private fun consumeWhile(predicate: (Char) -> Boolean) {
        tokenEnd = tokenStart + 1
        while (tokenEnd < endOffset && predicate(buffer[tokenEnd])) tokenEnd++
    }

    private fun single(type: IElementType) {
        tokenEnd = tokenStart + 1
        tokenType = type
    }

    private fun followsFunctionDeclaration(): Boolean = previousWord() == "fn"

    private fun followsDot(): Boolean {
        var index = tokenStart - 1
        while (index >= 0 && buffer[index].isWhitespace()) index--
        return index >= 0 && buffer[index] == '.'
    }

    private fun isLineLeadingIdentifier(): Boolean {
        var index = tokenStart - 1
        while (index >= 0 && buffer[index] != '\n' && buffer[index] != '\r') {
            if (!buffer[index].isWhitespace()) return false
            index--
        }
        return true
    }

    private fun previousWord(): String? {
        var end = tokenStart - 1
        while (end >= 0 && buffer[end].isWhitespace()) end--
        if (end < 0 || !buffer[end].isIdentifierPart()) return null
        var start = end
        while (start > 0 && buffer[start - 1].isIdentifierPart()) start--
        return buffer.subSequence(start, end + 1).toString()
    }

    private fun nextNonWhitespace(): Char? {
        var index = tokenEnd
        while (index < endOffset && buffer[index].isWhitespace()) index++
        return buffer.getOrNull(index)
    }

    private fun startsWith(text: String, offset: Int = tokenStart): Boolean =
        offset + text.length <= endOffset && buffer.subSequence(offset, offset + text.length).toString() == text

    private fun Char.isIdentifierStart(): Boolean = isLetter() || this == '_' || this == '$'

    private fun Char.isIdentifierPart(): Boolean = isLetterOrDigit() || this == '_' || this == '$'

    private companion object {
        val booleanLiterals = setOf("true", "false")
        val keywords = setOf(
            "alias", "break", "case", "const", "const_assert", "continue", "continuing", "default", "diagnostic",
            "discard", "else", "enable", "fn", "for", "if", "let", "loop", "override", "requires", "return",
            "struct", "switch", "var", "while",
        )
        val weslKeywords = setOf("as", "import", "package", "public", "self", "super")
        val addressSpaces = setOf("function", "private", "workgroup", "uniform", "storage", "handle")
        val accessModes = setOf("read", "write", "read_write")
        val textureFormats = setOf(
            "r8unorm", "r8snorm", "r8uint", "r8sint",
            "r16uint", "r16sint", "r16float",
            "r32uint", "r32sint", "r32float",
            "rg8unorm", "rg8snorm", "rg8uint", "rg8sint",
            "rg16uint", "rg16sint", "rg16float",
            "rg32uint", "rg32sint", "rg32float",
            "rgba8unorm", "rgba8unorm-srgb", "rgba8snorm", "rgba8uint", "rgba8sint",
            "rgba16uint", "rgba16sint", "rgba16float",
            "rgba32uint", "rgba32sint", "rgba32float",
            "bgra8unorm", "bgra8unorm-srgb", "rgb10a2uint", "rgb10a2unorm",
            "rg11b10ufloat", "rgb9e5ufloat",
        )
        val builtinTypes = setOf(
            "array", "atomic", "bool", "f16", "f32", "i32", "u32", "i64", "u64", "f64", "ptr",
            "sampler", "sampler_comparison", "texture_external",
        )
        val vectorOrMatrixType = Regex("(?:vec[2-4](?:[iufh])?|mat[2-4]x[2-4](?:[fh])?)")
        const val numberCharacters = "0123456789abcdefABCDEFxXpPeEuUiIfFhH.+-"
    }
}
