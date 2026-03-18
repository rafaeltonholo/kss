package dev.tonholo.kss.demo.ui

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type

/**
 * Processes a key event and returns the corresponding [KeyAction], or `null` if unhandled.
 *
 * Recognizes platform-aware modifier keys: Ctrl on non-macOS, Cmd (Meta) on macOS.
 *
 * @param event The key event to process.
 * @param isMacOs Whether the current platform is macOS.
 * @return The matched [KeyAction], or `null`.
 */
fun handleKeyEvent(event: KeyEvent, isMacOs: Boolean): KeyAction? {
    if (event.type != KeyEventType.KeyDown) return null

    val isPrimaryModifier = if (isMacOs) event.isMetaPressed else event.isCtrlPressed

    return when {
        // Ctrl+Shift+F / Cmd+Shift+F -> toggle AST filter
        isPrimaryModifier && event.isShiftPressed && event.key == Key.F -> KeyAction.ToggleAstFilter

        // Ctrl+F / Cmd+F -> toggle editor search
        isPrimaryModifier && event.key == Key.F -> KeyAction.ToggleEditorSearch

        // Enter -> next search match
        event.key == Key.Enter && !event.isShiftPressed -> KeyAction.NextSearchMatch

        // Shift+Enter -> previous search match
        event.key == Key.Enter && event.isShiftPressed -> KeyAction.PreviousSearchMatch

        // Escape -> close/clear
        event.key == Key.Escape -> KeyAction.Escape

        else -> null
    }
}

/**
 * The set of keyboard actions recognized by the app.
 */
enum class KeyAction {
    ToggleEditorSearch,
    ToggleAstFilter,
    NextSearchMatch,
    PreviousSearchMatch,
    Escape,
}

/**
 * Detects whether the current platform is macOS.
 *
 * On JVM, reads `System.getProperty("os.name")`.
 * On wasmJs, inspects `navigator.platform` via [kotlinx.browser.window].
 */
expect fun isMacOs(): Boolean
