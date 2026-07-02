package app.rafiq.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SurahJson(
    val number:       Int,
    @SerialName("name_ar")      val nameAr:       String,
    @SerialName("name_en")      val nameEn:       String,
    @SerialName("name_translit") val nameTranslit: String,
    val revelation:   String,
    @SerialName("ayah_count")   val ayahCount:    Int,
    @SerialName("juz_start")    val juzStart:     Int,
    @SerialName("page_start")   val pageStart:    Int
)

@Serializable
data class AyahJson(
    val surah:        Int,
    val ayah:         Int,
    @SerialName("text_uthmani") val textUthmani:  String,
    @SerialName("text_simple")  val textSimple:   String,
    val juz:          Int,
    val hizb:         Int,
    val page:         Int
)

@Serializable
data class DhikrJson(
    @SerialName("text_ar")      val textAr:       String,
    val source:       String,
    @SerialName("source_grade") val sourceGrade:  String,
    val virtue:       String    = "",
    val count:        Int,
    @SerialName("audio_file")   val audioFile:    String? = null,
    @SerialName("sort_order")   val sortOrder:    Int
)

@Serializable
data class DuaJson(
    val category:     String,
    val occasion:     String    = "",
    @SerialName("text_ar")      val textAr:       String,
    val source:       String,
    @SerialName("source_grade") val sourceGrade:  String,
    @SerialName("sort_order")   val sortOrder:    Int
)

@Serializable
data class KhatiraJson(
    @SerialName("day_of_year")      val dayOfYear:      Int,
    @SerialName("verse_or_hadith")  val verseOrHadith:  String,
    val source:       String,
    val reflection:   String,
    val season:       String    = "normal",
    val reviewed:     Boolean   = false,
    val reviewer:     String?   = null
)

@kotlinx.serialization.Serializable
data class TafsirJson(
    val surah: Int,
    val ayah:  Int,
    val text:  String
)
