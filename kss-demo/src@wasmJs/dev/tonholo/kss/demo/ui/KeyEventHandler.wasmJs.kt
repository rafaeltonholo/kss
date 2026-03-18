package dev.tonholo.kss.demo.ui

import kotlinx.browser.window

/**
 * Detects whether the browser is running on macOS by inspecting [window.navigator.platform].
 */
actual fun isMacOs(): Boolean =
    window.navigator.platform.contains("Mac", ignoreCase = true)
