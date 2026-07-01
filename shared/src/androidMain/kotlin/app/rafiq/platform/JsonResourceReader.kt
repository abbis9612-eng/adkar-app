package app.rafiq.platform

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual class JsonResourceReader(private val context: Context) {
    actual suspend fun readAsset(fileName: String): String {
        return withContext(Dispatchers.IO) {
            context.assets.open(fileName)
                .bufferedReader()
                .use { it.readText() }
        }
    }
}
