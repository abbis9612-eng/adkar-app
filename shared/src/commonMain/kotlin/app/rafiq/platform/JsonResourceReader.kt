package app.rafiq.platform

expect class JsonResourceReader {
    suspend fun readAsset(fileName: String): String
}
