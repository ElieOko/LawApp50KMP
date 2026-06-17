package emy.partners.lawapp

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun convertMillisToDate(millis: Long): String {
    val instant = Instant.fromEpochMilliseconds(millis)
    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

    val day = dateTime.date.dayOfMonth.toString().padStart(2, '0')
    val month = dateTime.date.monthNumber.toString().padStart(2, '0')
    val year = dateTime.date.year

    return "$day/$month/$year"
}