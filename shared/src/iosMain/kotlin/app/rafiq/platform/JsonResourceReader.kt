package app.rafiq.platform

actual class JsonResourceReader {
    actual suspend fun readAsset(fileName: String): String {
        val name = fileName.substringBeforeLast(".")
        val ext  = fileName.substringAfterLast(".")
        // iOS: read from main bundle
        // Requires platform.Foundation imports for full implementation
        error("iOS JsonResourceReader not yet implemented: $name.$ext")
    }
}
