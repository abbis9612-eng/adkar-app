package app.rafiq.domain.model

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

actual class PrayerTimeCalculator actual constructor() {

    private val httpClient = HttpClient()

    private fun parseTime(timeStr: String, date: LocalDate): Long {
        val cleanTime = timeStr.substringBefore(" ") 
        val parts = cleanTime.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val localDateTime = LocalDateTime(date.year, date.monthNumber, date.dayOfMonth, hour, minute, 0, 0)
        return localDateTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
    }

    private fun elevationCorrectionMinutes(elevationMeters: Double): Int {
        if (elevationMeters <= 0.0) return 0
        val seconds = (elevationMeters / 100.0) * 37.5
        return (seconds / 60.0).toInt()
    }

    private fun getApiMethod(methodName: String): Int {
        return when (methodName) {
            "mwl" -> 3
            "umm_al_qura" -> 4
            "egyptian" -> 5
            "isna" -> 2
            "karachi" -> 1
            "turkey" -> 13
            else -> 3
        }
    }

    private fun getApiSchool(madhab: String): Int {
        return if (madhab == "hanafi") 1 else 0
    }

    private suspend fun fetchFromApi(
        lat: Double, lng: Double, methodStr: String, 
        elevation: Double, madhab: String,
        fajrOffset: Int, dhuhrOffset: Int, asrOffset: Int,
        maghribOffset: Int, ishaOffset: Int,
        addDays: Int
    ): PrayerTimesResult {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val targetDate = if (addDays > 0) today.plus(addDays, DateTimeUnit.DAY) else today

        val dayStr = targetDate.dayOfMonth.toString().padStart(2, '0')
        val monthStr = targetDate.monthNumber.toString().padStart(2, '0')
        val yearStr = targetDate.year.toString()
        val dateStr = "$dayStr-$monthStr-$yearStr"

        val methodId = getApiMethod(methodStr)
        val schoolId = getApiSchool(madhab)
        val url = "https://api.aladhan.com/v1/timings/$dateStr?latitude=$lat&longitude=$lng&method=$methodId&school=$schoolId"
        
        val response = httpClient.get(url)
        val responseText = response.bodyAsText()
        val json = Json.parseToJsonElement(responseText).jsonObject
        val timings = json["data"]!!.jsonObject["timings"]!!.jsonObject
        
        val elevCorr = elevationCorrectionMinutes(elevation)

        // Add offsets + elevation corrections manually (converted to milliseconds)
        val msFajr = (fajrOffset) * 60_000L
        val msSunrise = (-elevCorr) * 60_000L
        val msDhuhr = (dhuhrOffset) * 60_000L
        val msAsr = (asrOffset) * 60_000L
        val msMaghrib = (maghribOffset + elevCorr) * 60_000L
        val msIsha = (ishaOffset) * 60_000L

        return PrayerTimesResult(
            fajr    = parseTime(timings["Fajr"]!!.jsonPrimitive.content, targetDate) + msFajr,
            sunrise = parseTime(timings["Sunrise"]!!.jsonPrimitive.content, targetDate) + msSunrise,
            dhuhr   = parseTime(timings["Dhuhr"]!!.jsonPrimitive.content, targetDate) + msDhuhr,
            asr     = parseTime(timings["Asr"]!!.jsonPrimitive.content, targetDate) + msAsr,
            maghrib = parseTime(timings["Maghrib"]!!.jsonPrimitive.content, targetDate) + msMaghrib,
            isha    = parseTime(timings["Isha"]!!.jsonPrimitive.content, targetDate) + msIsha
        )
    }

    actual suspend fun calculate(
        lat: Double, lng: Double, method: String,
        elevation: Double, madhab: String,
        fajrOffset: Int, dhuhrOffset: Int, asrOffset: Int,
        maghribOffset: Int, ishaOffset: Int
    ): PrayerTimesResult {
        return fetchFromApi(
            lat, lng, method, elevation, madhab, 
            fajrOffset, dhuhrOffset, asrOffset, maghribOffset, ishaOffset, 
            0
        )
    }

    actual suspend fun calculateForTomorrow(
        lat: Double, lng: Double, method: String,
        elevation: Double, madhab: String,
        fajrOffset: Int, dhuhrOffset: Int, asrOffset: Int,
        maghribOffset: Int, ishaOffset: Int
    ): PrayerTimesResult {
        return fetchFromApi(
            lat, lng, method, elevation, madhab, 
            fajrOffset, dhuhrOffset, asrOffset, maghribOffset, ishaOffset, 
            1
        )
    }
}
