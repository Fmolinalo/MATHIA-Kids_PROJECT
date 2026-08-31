package com.example.mathia.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mathia.data.repository.CalendarRepository
import com.example.mathia.model.CalendarEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class CalendarUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val events: List<CalendarEvent> = emptyList(),
    val isLoading: Boolean = false,
    val isGoogleConnected: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val calendarRepository: CalendarRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    fun loadMonthEvents(context: Context, yearMonth: YearMonth, studentAsistencia: List<String>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                currentMonth = yearMonth,
                isLoading = true,
                errorMessage = null
            )

            try {
                val isConnected = calendarRepository.isGoogleConnected(context)
                val eventList = calendarRepository.getEventsForMonth(context, yearMonth, studentAsistencia)

                _uiState.value = _uiState.value.copy(
                    events = eventList,
                    isGoogleConnected = isConnected,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "No pudimos actualizar tu calendario."
                )
            }
        }
    }

    fun selectDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
    }

    fun goToPreviousMonth(context: Context, studentAsistencia: List<String>) {
        val prevMonth = _uiState.value.currentMonth.minusMonths(1)
        loadMonthEvents(context, prevMonth, studentAsistencia)
    }

    fun goToNextMonth(context: Context, studentAsistencia: List<String>) {
        val nextMonth = _uiState.value.currentMonth.plusMonths(1)
        loadMonthEvents(context, nextMonth, studentAsistencia)
    }

    fun goToToday(context: Context, studentAsistencia: List<String>) {
        val nowMonth = YearMonth.now()
        val today = LocalDate.now()
        _uiState.value = _uiState.value.copy(selectedDate = today)
        loadMonthEvents(context, nowMonth, studentAsistencia)
    }
}
