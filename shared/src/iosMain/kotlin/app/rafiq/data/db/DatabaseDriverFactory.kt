package app.rafiq.data.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import app.rafiq.db.RafiqDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = RafiqDatabase.Schema,
            name   = "rafiq.db"
        )
    }
}
