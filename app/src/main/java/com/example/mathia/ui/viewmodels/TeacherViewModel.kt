package com.example.mathia.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mathia.FirebaseRepository
import com.example.mathia.model.Classroom
import com.example.mathia.model.JoinRequest
import com.example.mathia.model.Observacion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeacherViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository
) : ViewModel() {

    private val _classrooms = MutableStateFlow<List<Classroom>>(emptyList())
    val classrooms: StateFlow<List<Classroom>> = _classrooms

    private val _pendingRequests = MutableStateFlow<List<JoinRequest>>(emptyList())
    val pendingRequests: StateFlow<List<JoinRequest>> = _pendingRequests

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadTeacherData(teacherId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _classrooms.value = firebaseRepository.obtenerSalonesProfesor(teacherId)
            _pendingRequests.value = firebaseRepository.obtenerSolicitudesPendientesProfesor(teacherId)
            _isLoading.value = false
        }
    }

    fun createClassroom(
        teacherId: String,
        teacherName: String,
        name: String,
        grade: String,
        section: String,
        school: String,
        onSuccess: (Classroom) -> Unit
    ) {
        viewModelScope.launch {
            val created = firebaseRepository.crearSalom(teacherId, teacherName, name, grade, section, school)
            if (created != null) {
                _classrooms.value = _classrooms.value + created
                onSuccess(created)
            }
        }
    }

    fun respondToJoinRequest(requestId: String, teacherId: String, approve: Boolean) {
        viewModelScope.launch {
            val status = if (approve) "APPROVED" else "REJECTED"
            val success = firebaseRepository.responderSolicitudUnion(requestId, status)
            if (success) {
                _pendingRequests.value = _pendingRequests.value.filter { it.requestId != requestId }
            }
        }
    }

    fun sendObservation(teacherId: String, teacherName: String, studentId: String, text: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val obs = Observacion(
                id = "",
                docenteId = teacherId,
                docenteNombre = teacherName,
                texto = text,
                fecha = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
                timestamp = System.currentTimeMillis()
            )
            val success = firebaseRepository.guardarObservacion(studentId, obs)
            onComplete(success)
        }
    }
}
