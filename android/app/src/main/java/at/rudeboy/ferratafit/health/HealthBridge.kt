package at.rudeboy.ferratafit.health

import android.content.Context
import android.content.Intent
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import at.rudeboy.ferratafit.data.Session
import java.time.Instant
import java.time.ZoneId

/**
 * Brücke zu Samsung Health.
 *
 * Samsung bietet zwar ein eigenes SDK an, das aber eine Partnerfreigabe verlangt und
 * damit für eine private App ausscheidet. Der offene Weg führt über Health Connect:
 * Samsung Health synchronisiert Training, Schritte und Herzfrequenz bidirektional
 * damit. Schreibt die App also eine Einheit nach Health Connect, taucht sie kurz
 * darauf auch in Samsung Health auf — der Abgleich läuft periodisch, typischerweise
 * innerhalb einer Stunde, nicht sekundengenau.
 */
class HealthBridge(private val context: Context) {

    companion object {
        private const val HEALTH_CONNECT_PACKAGE = "com.google.android.apps.healthdata"

        val PERMISSIONS: Set<String> = setOf(
            HealthPermission.getWritePermission(ExerciseSessionRecord::class),
            HealthPermission.getReadPermission(ExerciseSessionRecord::class),
            HealthPermission.getReadPermission(StepsRecord::class)
        )
    }

    /** Verfügbarkeit von Health Connect auf diesem Gerät. */
    enum class Availability { READY, NEEDS_UPDATE, UNSUPPORTED }

    fun availability(): Availability = when (HealthConnectClient.getSdkStatus(context, HEALTH_CONNECT_PACKAGE)) {
        HealthConnectClient.SDK_AVAILABLE -> Availability.READY
        HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> Availability.NEEDS_UPDATE
        else -> Availability.UNSUPPORTED
    }

    private fun clientOrNull(): HealthConnectClient? = try {
        if (availability() == Availability.READY) HealthConnectClient.getOrCreate(context) else null
    } catch (_: Exception) {
        null
    }

    suspend fun hasPermissions(): Boolean {
        val client = clientOrNull() ?: return false
        return try {
            client.permissionController.getGrantedPermissions().containsAll(PERMISSIONS)
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Schreibt eine Trainingseinheit als Krafttraining nach Health Connect.
     *
     * Die Session-ID der App wandert als clientRecordId mit. Health Connect erkennt daran
     * Wiederholungen, sodass ein zweiter Schreibversuch denselben Eintrag aktualisiert,
     * statt ein Duplikat anzulegen.
     */
    suspend fun writeSession(session: Session, title: String): Result<Unit> {
        val client = clientOrNull()
            ?: return Result.failure(IllegalStateException("Health Connect ist auf diesem Gerät nicht verfügbar."))

        return try {
            val zone = ZoneId.systemDefault()
            val start = Instant.ofEpochMilli(session.startedAt)
            val end = Instant.ofEpochMilli(session.finishedAt.coerceAtLeast(session.startedAt + 60_000L))

            val record = ExerciseSessionRecord(
                startTime = start,
                startZoneOffset = zone.rules.getOffset(start),
                endTime = end,
                endZoneOffset = zone.rules.getOffset(end),
                exerciseType = ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING,
                title = title,
                notes = buildString {
                    append("${session.sets.size} Sätze")
                    if (session.volumeKg > 0) append(" · ${session.volumeKg.toInt()} kg Gesamtlast")
                    if (session.note.isNotBlank()) append(" · ${session.note}")
                },
                metadata = Metadata.manualEntry(clientRecordId = session.id)
            )
            client.insertRecords(listOf(record))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Schritte des heutigen Tages — kommen bei Samsung-Nutzern von der Uhr bzw. dem Handy. */
    suspend fun stepsToday(): Long? {
        val client = clientOrNull() ?: return null
        return try {
            val zone = ZoneId.systemDefault()
            val startOfDay = Instant.now().atZone(zone).toLocalDate().atStartOfDay(zone).toInstant()
            val response = client.aggregate(
                AggregateRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startOfDay, Instant.now())
                )
            )
            response[StepsRecord.COUNT_TOTAL]
        } catch (_: Exception) {
            null
        }
    }

    /** Öffnet den Play-Store-Eintrag, falls Health Connect fehlt oder veraltet ist. */
    fun installIntent(): Intent = Intent(Intent.ACTION_VIEW).apply {
        data = android.net.Uri.parse(
            "market://details?id=$HEALTH_CONNECT_PACKAGE&url=healthconnect%3A%2F%2Fonboarding"
        )
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        putExtra("overlay", true)
        putExtra("callerId", context.packageName)
    }

    /** Öffnet die Health-Connect-Einstellungen, damit man Freigaben nachjustieren kann. */
    fun settingsIntent(): Intent =
        Intent(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
}
