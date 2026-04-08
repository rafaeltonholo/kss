package dev.tonholo.kss.extensions

/**
 * Prepends an indent to the string.
 */
fun String.prependIndent(indentSize: Int) = prependIndent(" ".repeat(indentSize))
