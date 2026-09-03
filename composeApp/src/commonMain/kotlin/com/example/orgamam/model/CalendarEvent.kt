package com.example.orgamam.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class CalendarEvent(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val dateTimestamp: Long = 0,
    val authorId: String = "",
    val authorName: String = "",
    val authorHexColor: String = "#6200EE"
) {
    val date: LocalDate
        get() = Instant.fromEpochMilliseconds(dateTimestamp)
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
}
