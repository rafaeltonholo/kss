package dev.tonholo.kss.lexer

/**
 * Represents a kind of token.
 */
interface TokenKind {
    /**
     * The characters that represent this token kind.
     */
    val representation: Set<Char>

    /**
     * Checks if a character is part of this token kind's representation.
     */
    operator fun contains(other: Char): Boolean = other in representation
}
