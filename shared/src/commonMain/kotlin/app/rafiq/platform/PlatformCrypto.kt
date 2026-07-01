package app.rafiq.platform

expect object PlatformCrypto {
    fun sha256(input: String): String
}

object QuranIntegrity {
    private const val EXPECTED_HASH = "REPLACE_WITH_ACTUAL_SHA256_AT_BUILD_TIME"

    fun verify(quranJson: String): Boolean =
        PlatformCrypto.sha256(quranJson) == EXPECTED_HASH
}
