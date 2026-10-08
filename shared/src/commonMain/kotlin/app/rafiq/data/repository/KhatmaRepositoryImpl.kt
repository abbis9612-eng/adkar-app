package app.rafiq.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import app.rafiq.db.RafiqDatabase
import app.rafiq.domain.model.KhatmaPlan
import app.rafiq.domain.repository.KhatmaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class KhatmaRepositoryImpl(private val db: RafiqDatabase) : KhatmaRepository {

    private fun today(): Long = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()).date.toEpochDays().toLong()

    override fun active(): Flow<KhatmaPlan?> =
        db.khatmaQueries.getActive().asFlow().mapToOneOrNull(Dispatchers.IO)
            .map { it?.toPlan() }

    override fun all(): Flow<List<KhatmaPlan>> =
        db.khatmaQueries.getAll().asFlow().mapToList(Dispatchers.IO)
            .map { rows -> rows.map { it.toPlan() } }

    override suspend fun start(
        days: Int,
        fromPage: Int,
        toPage: Int,
        meeqat: String,
        continuous: Boolean,
    ) = withContext(Dispatchers.IO) {
        db.transaction {
            db.khatmaQueries.deactivateAll()
            db.khatmaQueries.insert(
                started_on = today(),
                days = days.coerceIn(1, 365).toLong(),
                from_page = fromPage.coerceIn(1, 604).toLong(),
                to_page = toPage.coerceIn(1, 604).toLong(),
                meeqat = meeqat,
                continuous = if (continuous) 1L else 0L,
            )
        }
    }

    /*  لا يَنقُص.
     *
     *  من رجع يراجع صفحةً قديمةً لا يُمحى تقدّمُه — والمراجعةُ لا تُلغي
     *  ما قُرئ. فيُؤخذ الأكبرُ دائماً. */
    override suspend fun markRead(page: Int) = withContext(Dispatchers.IO) {
        val k = db.khatmaQueries.getActive().executeAsOneOrNull() ?: return@withContext
        val next = maxOf(k.read_to, page.toLong().coerceIn(0, k.to_page))
        if (next != k.read_to) db.khatmaQueries.updateReadTo(next, k.id)
    }

    /*  الختمُ يُنهي النشطة. والمستمرّةُ تبدأ غيرَها بالمواصفات نفسِها في
     *  المعاملة نفسِها — فلا تمرّ لحظةٌ بلا ختمةٍ على من اختار الاستمرار. */
    override suspend fun finish() = withContext(Dispatchers.IO) {
        val k = db.khatmaQueries.getActive().executeAsOneOrNull() ?: return@withContext
        db.transaction {
            db.khatmaQueries.finish(today(), k.id)
            if (k.continuous == 1L) {
                db.khatmaQueries.insert(
                    started_on = today(),
                    days = k.days,
                    from_page = k.from_page,
                    to_page = k.to_page,
                    meeqat = k.meeqat,
                    continuous = 1L,
                )
            }
        }
    }

    override suspend fun remove(id: Long) = withContext(Dispatchers.IO) {
        db.khatmaQueries.deleteById(id)
    }
}

private fun app.rafiq.db.Khatma.toPlan() = KhatmaPlan(
    id = id,
    startedOn = started_on,
    days = days.toInt(),
    fromPage = from_page.toInt(),
    toPage = to_page.toInt(),
    readTo = read_to.toInt(),
    meeqat = meeqat,
    continuous = continuous == 1L,
    active = active == 1L,
    finishedOn = finished_on,
)
