package app.rafiq.data.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.Serializable

class RafiqApi(private val client: HttpClient) {
    companion object { private const val BASE_URL = "https://api.rafiqaldhikr.app/v1" }

    suspend fun getKhatira(dayOfYear: Int): Result<KhatiraDto>       = runCatching { client.get("$BASE_URL/khatira/$dayOfYear").body() }
    suspend fun getAppConfig(): Result<AppConfigDto>                  = runCatching { client.get("$BASE_URL/config").body() }
    suspend fun getContentVersion(): Result<ContentVersionDto>        = runCatching { client.get("$BASE_URL/content/version").body() }
}

@Serializable data class KhatiraDto(val dayOfYear: Int, val text: String, val source: String, val reflection: String, val version: String)
@Serializable data class AppConfigDto(val minVersion: String, val maintenanceMode: Boolean, val message: String?)
@Serializable data class ContentVersionDto(val adhkarVersion: String, val khatiraVersion: String)
