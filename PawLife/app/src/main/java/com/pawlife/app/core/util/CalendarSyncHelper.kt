package com.pawlife.app.core.util

import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarSyncHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun insertEvent(
        title: String,
        description: String,
        location: String,
        startTimeMs: Long,
        endTimeMs: Long
    ): Long? {
        val resolver = context.contentResolver

        // 1. Find a valid calendar ID. Defaulting to Primary (or first available).
        var calendarId: Long = -1L
        val projection = arrayOf(CalendarContract.Calendars._ID, CalendarContract.Calendars.IS_PRIMARY)
        val uri = CalendarContract.Calendars.CONTENT_URI
        val cursor = resolver.query(uri, projection, null, null, null)

        cursor?.use {
            if (it.moveToFirst()) {
                val idCol = it.getColumnIndex(CalendarContract.Calendars._ID)
                val primaryCol = it.getColumnIndex(CalendarContract.Calendars.IS_PRIMARY)

                do {
                    val id = it.getLong(idCol)
                    val isPrimary = if (primaryCol != -1) it.getInt(primaryCol) == 1 else false
                    if (isPrimary) {
                        calendarId = id
                        break
                    }
                    if (calendarId == -1L) {
                        calendarId = id // Fallback to first found
                    }
                } while (it.moveToNext())
            }
        }

        if (calendarId == -1L) return null

        // 2. Insert Event
        val values = ContentValues().apply {
            put(CalendarContract.Events.DTSTART, startTimeMs)
            put(CalendarContract.Events.DTEND, endTimeMs)
            put(CalendarContract.Events.TITLE, title)
            put(CalendarContract.Events.DESCRIPTION, description)
            put(CalendarContract.Events.EVENT_LOCATION, location)
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
        }

        return try {
            val eventUri = resolver.insert(CalendarContract.Events.CONTENT_URI, values)
            eventUri?.lastPathSegment?.toLongOrNull()
        } catch (e: SecurityException) {
            e.printStackTrace()
            null
        }
    }
}
