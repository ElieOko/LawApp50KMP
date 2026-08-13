package emy.partners.lawapp.data.remote.evaluation

import kotlinx.datetime.Clock

fun nowEpochMs(): Long = Clock.System.now().toEpochMilliseconds()

fun resolveCompteurMinutes(vararg values: Long?): Long =
    values.mapNotNull { it }.firstOrNull { it > 0L } ?: 0L

fun remainingSeconds(
    startedAtEpochMs: Long,
    durationMinutes: Long,
    nowMs: Long = nowEpochMs(),
): Long {
    if (durationMinutes <= 0L || startedAtEpochMs <= 0L) return -1L
    val totalMs = durationMinutes * 60_000L
    val elapsed = (nowMs - startedAtEpochMs).coerceAtLeast(0L)
    return ((totalMs - elapsed) / 1_000L).coerceAtLeast(0L)
}

fun formatCountdown(totalSeconds: Long): String {
    val safe = totalSeconds.coerceAtLeast(0L)
    val hours = safe / 3_600L
    val minutes = (safe % 3_600L) / 60L
    val seconds = safe % 60L
    val mm = minutes.toString().padStart(2, '0')
    val ss = seconds.toString().padStart(2, '0')
    return if (hours > 0L) {
        "$hours:$mm:$ss"
    } else {
        "$mm:$ss"
    }
}

fun EvaluationTakeSheet.withFallbackMinutes(fallbackMinutes: Long): EvaluationTakeSheet {
    val minutes = resolveCompteurMinutes(compteurMinutes, fallbackMinutes)
    return if (minutes == compteurMinutes) this else copy(compteurMinutes = minutes)
}
