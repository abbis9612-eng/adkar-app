package app.rafiqaldhikr.ui.mushaf

import android.content.Context
import org.json.JSONArray

/** أسماءُ السور من `surah_metadata.json` — تُقرأ مرّةً وتُحفظ. */
object SurahNames {
    @Volatile private var names: List<String>? = null

    fun of(context: Context, surah: Int): String {
        val list = names ?: synchronized(this) { names ?: load(context).also { names = it } }
        return list.getOrNull(surah - 1).orEmpty()
    }

    private fun load(context: Context): List<String> = runCatching {
        val arr = JSONArray(
            context.assets.open("surah_metadata.json").bufferedReader().use { it.readText() },
        )
        List(arr.length()) { arr.getJSONObject(it).optString("name_ar") }
    }.getOrDefault(emptyList())
}
