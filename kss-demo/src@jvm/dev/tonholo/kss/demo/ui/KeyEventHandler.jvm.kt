package dev.tonholo.kss.demo.ui

actual fun isMacOs(): Boolean {
    return System.getProperty("os.name").orEmpty().contains("Mac", ignoreCase = true)
}
