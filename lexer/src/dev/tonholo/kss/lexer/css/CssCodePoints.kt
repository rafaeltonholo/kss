package dev.tonholo.kss.lexer.css

private const val NON_ASCII_START = 0x80

/**
 * CSS Syntax Module Level 3, §4.2 "ident-start code point":
 * a letter, `_`, or a non-ASCII code point (>= U+0080).
 *
 * See: https://www.w3.org/TR/css-syntax-3/#ident-start-code-point
 */
internal fun Char.isIdentStartCodePoint(): Boolean =
    this in 'a'..'z' ||
        this in 'A'..'Z' ||
        this == '_' ||
        code >= NON_ASCII_START

/**
 * CSS Syntax Module Level 3, §4.2 "ident code point":
 * an ident-start code point, a digit, or `-`.
 *
 * See: https://www.w3.org/TR/css-syntax-3/#ident-code-point
 */
internal fun Char.isIdentCodePoint(): Boolean =
    isIdentStartCodePoint() ||
        this in '0'..'9' ||
        this == '-'
