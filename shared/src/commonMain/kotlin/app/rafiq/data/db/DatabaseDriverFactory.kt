package app.rafiq.data.db

import app.cash.sqldelight.db.SqlDriver
import app.rafiq.db.RafiqDatabase

expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}

fun createDatabase(driverFactory: DatabaseDriverFactory): RafiqDatabase {
    val driver = driverFactory.createDriver()
    return RafiqDatabase(driver)
}
