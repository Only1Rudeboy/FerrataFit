package at.rudeboy.ferratafit.health

import android.content.Context
import android.content.Intent
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.BasalMetabolicRateRecord
import androidx.health.connect.client.records.BodyFatRecord
import androidx.health.connect.client.records.BodyWaterMassRecord
import androidx.health.connect.client.records.BoneMassRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeightRecord
import androidx.health.connect.client.records.LeanBodyMassRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import at.rudeboy.ferratafit.data.BodyMeasurement
import at.rudeboy.ferratafit.data.Session
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.reflect.KClass

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
            HealthPermission.getReadPermission(StepsRecord::class),
            // Körperdaten von der Waage — kommen über Samsung Health aus FitDays herein
            HealthPermission.getReadPermission(WeightRecord::class),
            HealthPermission.getReadPermission(BodyFatRecord::class),
            HealthPermission.getReadPermission(LeanBodyMassRecord::class),
            HealthPermission.getReadPermission(BoneMassRecord::class),
            HealthPermission.getReadPermission(BodyWaterMassRecord::class),
            HealthPermission.getReadPermission(BasalMetabolicRateRecord::class),
            HealthPermission.getReadPermission(HeightRecord::class)
        )

        /**
         * Die Körperdaten sind optional: Wer keine vernetzte Waage hat, soll die
         * Freigabe dafür nicht erteilen müssen. Deshalb werden sie getrennt geprüft.
         */
        val BODY_PERMISSIONS: Set<String> = setOf(
            HealthPermission.getReadPermission(WeightRecord::class),
            HealthPermission.getReadPermission(BodyFatRecord::class),
            HealthPermission.getReadPermission(LeanBodyMassRecord::class),
            HealthPermission.getReadPermission(BoneMassRecord::class),
            HealthPermission.getReadPermission(BodyWaterMassRecord::class)
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

    /** Sind die Freigaben für die Körperdaten erteilt? */
    suspend fun hasBodyPermissions(): Boolean {
        val client = clientOrNull() ?: return false
        return try {
            client.permissionController.getGrantedPermissions()
                .contains(HealthPermission.getReadPermission(WeightRecord::class))
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Liest die Körperdaten der letzten [days] Tage aus Health Connect.
     *
     * Die einzelnen Werte liegen dort als getrennte Einträge vor — Gewicht, Fettanteil,
     * Magermasse und so weiter kommen jeweils einzeln an. Eine Wägung erzeugt aber
     * mehrere davon mit praktisch demselben Zeitstempel. Deshalb wird zu jeder Wägung
     * der zeitlich nächste Eintrag gesucht, sofern er höchstens fünf Minuten entfernt ist.
     */
    suspend fun readBody(days: Int = 400): List<BodyMeasurement> {
        val client = clientOrNull() ?: return emptyList()
        val end = Instant.now()
        val start = end.minus(days.toLong(), ChronoUnit.DAYS)
        val range = TimeRangeFilter.between(start, end)

        suspend fun <T : Record> read(type: KClass<T>): List<T> = try {
            client.readRecords(ReadRecordsRequest(type, range)).records
        } catch (_: Exception) {
            emptyList()
        }

        val weights = read(WeightRecord::class)
        if (weights.isEmpty()) return emptyList()

        val fats = read(BodyFatRecord::class)
        val lean = read(LeanBodyMassRecord::class)
        val bone = read(BoneMassRecord::class)
        val water = read(BodyWaterMassRecord::class)
        val basal = read(BasalMetabolicRateRecord::class)

        fun <T> nearest(items: List<T>, at: Instant, timeOf: (T) -> Instant): T? =
            items.minByOrNull { kotlin.math.abs(timeOf(it).toEpochMilli() - at.toEpochMilli()) }
                ?.takeIf {
                    kotlin.math.abs(timeOf(it).toEpochMilli() - at.toEpochMilli()) <= 5 * 60_000L
                }

        return weights
            .map { w ->
                BodyMeasurement(
                    at = w.time.toEpochMilli(),
                    weightKg = w.weight.inKilograms,
                    bodyFatPercent = nearest(fats, w.time) { it.time }?.percentage?.value,
                    leanMassKg = nearest(lean, w.time) { it.time }?.mass?.inKilograms,
                    boneMassKg = nearest(bone, w.time) { it.time }?.mass?.inKilograms,
                    waterMassKg = nearest(water, w.time) { it.time }?.mass?.inKilograms,
                    basalKcal = nearest(basal, w.time) { it.time }
                        ?.basalMetabolicRate?.inKilocaloriesPerDay?.toInt()
                )
            }
            // Mehrere Waegungen am selben Tag: die letzte gewinnt
            .groupBy { it.at / (24 * 60 * 60 * 1000L) }
            .map { (_, sameDay) -> sameDay.maxBy { it.at } }
            .sortedBy { it.at }
    }

    /** Die zuletzt hinterlegte Körpergröße in Zentimetern, falls vorhanden. */
    suspend fun readHeightCm(): Double? {
        val client = clientOrNull() ?: return null
        return try {
            val end = Instant.now()
            client.readRecords(
                ReadRecordsRequest(
                    HeightRecord::class,
                    TimeRangeFilter.between(end.minus(3650, ChronoUnit.DAYS), end)
                )
            ).records.maxByOrNull { it.time }?.height?.inMeters?.times(100)
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
