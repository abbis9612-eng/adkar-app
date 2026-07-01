package app.rafiq.platform

actual object PlatformCrypto {
    actual fun sha256(input: String): String {
        // iOS implementation using CommonCrypto
        // For now, a placeholder — full implementation requires kotlinx.cinterop
        return ""
    }
}
