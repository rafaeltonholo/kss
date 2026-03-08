package dev.tonholo.kss.demo.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import dev.tonholo.kss.demo.theme.SyntaxColors
import dev.tonholo.kss.lexer.Token
import dev.tonholo.kss.lexer.css.CssTokenKind

private val SkippedTokenKinds = setOf(CssTokenKind.EndOfFile, CssTokenKind.WhiteSpace)
private val ErrorUnderlineStyle =
    SpanStyle(
        textDecoration = TextDecoration.Underline,
        background = Color(0x40FF0000),
        color = Color(0xFFFF6B6B)
    )

/**
 * Converts plain CSS text and its token stream into a syntax-highlighted [AnnotatedString].
 */
object SyntaxHighlighter {
    /**
     * Builds an [AnnotatedString] with color spans for each token and an optional
     * error underline at the given [errorRange].
     *
     * @param text The raw CSS source text.
     * @param tokens The token stream produced by [CssTokenizer][dev.tonholo.kss.lexer.css.CssTokenizer].
     * @param errorRange An optional character offset range to highlight as an error.
     * @return A styled [AnnotatedString] ready for use with a [VisualTransformation][androidx.compose.ui.text.input.VisualTransformation].
     */
    fun highlight(
        text: String,
        tokens: List<Token<out CssTokenKind>>,
        errorRange: IntRange? = null,
    ): AnnotatedString =
        buildAnnotatedString {
            append(text)
            for (token in tokens) {
                if (token.kind in SkippedTokenKinds) continue
                val start = token.startOffset.coerceIn(0, text.length)
                val end = token.endOffset.coerceIn(start, text.length)
                if (start < end) {
                    addStyle(
                        style = SpanStyle(color = SyntaxColors.colorFor(token.kind)),
                        start = start,
                        end = end
                    )
                }
            }
            if (errorRange != null) {
                val start = errorRange.first.coerceIn(0, text.length)
                val end = (errorRange.last + 1).coerceIn(start, text.length)
                if (start < end) {
                    addStyle(style = ErrorUnderlineStyle, start = start, end = end)
                }
            }
        }
}
