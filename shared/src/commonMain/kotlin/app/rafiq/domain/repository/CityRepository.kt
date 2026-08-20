package app.rafiq.domain.repository

import app.rafiq.domain.model.City

interface CityRepository {
    suspend fun all(): List<City>
}
