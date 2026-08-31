package com.example.mathia.data.repository

import android.content.Context
import com.example.mathia.data.remote.GoogleCalendarDataSource
import com.example.mathia.model.CalendarEvent
import com.example.mathia.model.CalendarEventSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarRepository @Inject constructor(
    private val googleCalendarDataSource: GoogleCalendarDataSource
) {
    private val monthlyCache = ConcurrentHashMap<String, List<CalendarEvent>>()

    fun isGoogleConnected(context: Context): Boolean {
        return googleCalendarDataSource.isGoogleCalendarConnected(context)
    }

    suspend fun getEventsForMonth(
        context: Context,
        yearMonth: YearMonth,
        studentAsistencia: List<String>
    ): List<CalendarEvent> = withContext(Dispatchers.IO) {
        val cacheKey = "calendar_${yearMonth.year}_%02d".format(yearMonth.monthValue)

        // 1. MathIA activity events from student asistencia list
        val mathiaEvents = studentAsistencia.mapNotNull { dateStr ->
            try {
                val date = LocalDate.parse(dateStr)
                if (date.year == yearMonth.year && date.monthValue == yearMonth.monthValue) {
                    CalendarEvent(
                        id = "mathia_$dateStr",
                        title = "Práctica MathIA Completada",
                        description = "Sesión de práctica diaria realizada",
                        date = dateStr,
                        startTime = "",
                        endTime = "",
                        isAllDay = true,
                        source = CalendarEventSource.MATHIA
                    )
                } else null
            } catch (e: Exception) {
                null
            }
        }

        // 2. Fetch Google Calendar events (read-only)
        val googleEvents = googleCalendarDataSource.fetchEventsForMonth(context, yearMonth)

        val combinedEvents = (mathiaEvents + googleEvents).sortedBy { it.date }
        monthlyCache[cacheKey] = combinedEvents
        combinedEvents
    }

    fun clearCache() {
        monthlyCache.clear()
    }
}
