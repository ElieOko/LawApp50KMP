package emy.partners.lawapp

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime


fun convertMillisToDate(millis: Long): String {
    val instant = Instant.fromEpochMilliseconds(millis)

    val date = instant
        .toLocalDateTime(TimeZone.UTC)
        .date

    val day = date.dayOfMonth.toString().padStart(2, '0')
    val month = date.monthNumber.toString().padStart(2, '0')
    val year = date.year

    return "$day/$month/$year"
}