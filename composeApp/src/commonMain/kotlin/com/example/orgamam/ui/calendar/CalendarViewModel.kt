package com.example.orgamam.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.orgamam.model.CalendarEvent
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

data class CalendarUiState(
    val selectedDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val currentMonthDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()).let { LocalDate(it.year, it.month, 1) },
    val events: List<CalendarEvent> = emptyList(),
    val groupId: String? = null,
    val userName: String = "",
    val userColor: String = "#6200EE"
)

class CalendarViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private var eventsJob: Job? = null
    private var userJob: Job? = null

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadUserGroupAndEvents()
    }

    private fun loadUserGroupAndEvents() {
        val uid = auth.currentUser?.uid ?: return
        userJob = db.collection("users").document(uid).snapshots
            .onEach { snapshot ->
                val groupId = snapshot.get<String?>("groupId")
                val name = snapshot.get<String?>("displayName") ?: ""
                val color = snapshot.get<String?>("hexColor") ?: "#6200EE"
                
                _uiState.update { it.copy(
                    groupId = groupId,
                    userName = name,
                    userColor = color
                ) }
                
                if (groupId != null) {
                    listenToEvents(groupId)
                }
            }.launchIn(viewModelScope)
    }

    private fun listenToEvents(groupId: String) {
        eventsJob?.cancel()
        eventsJob = db.collection("groups").document(groupId).collection("events").snapshots
            .onEach { snapshot ->
                val events = snapshot.documents.map { it.data<CalendarEvent>() }
                _uiState.update { it.copy(events = events) }
            }.launchIn(viewModelScope)
    }

    fun addEvent(title: String, description: String, date: LocalDate) {
        val groupId = uiState.value.groupId ?: return
        val uid = auth.currentUser?.uid ?: return
        val eventId = Clock.System.now().toEpochMilliseconds().toString() // Simple ID
        
        // Convert LocalDate to timestamp
        val timestamp = date.toEpochDays().toLong() * 24 * 60 * 60 * 1000 // Inaccurate but consistent if we use same logic
        // Better: use kotlinx-datetime to get start of day
        
        val newEvent = CalendarEvent(
            id = eventId,
            title = title,
            description = description,
            dateTimestamp = timestamp, // We should probably store the day directly in KMP
            authorId = uid,
            authorName = uiState.value.userName,
            authorHexColor = uiState.value.userColor
        )

        viewModelScope.launch {
            db.collection("groups").document(groupId).collection("events")
                .document(eventId).set(newEvent)
        }
    }

    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun nextMonth() {
        _uiState.update { it.copy(currentMonthDate = it.currentMonthDate.plus(1, DateTimeUnit.MONTH)) }
    }

    fun previousMonth() {
        _uiState.update { it.copy(currentMonthDate = it.currentMonthDate.minus(1, DateTimeUnit.MONTH)) }
    }

    override fun onCleared() {
        super.onCleared()
        userJob?.cancel()
        eventsJob?.cancel()
    }
}
