package com.example.mathia.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mathia.FirebaseRepository
import com.example.mathia.model.FirebaseStudent
import com.example.mathia.model.JoinRequestResult
import com.example.mathia.model.Observacion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ParentViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository
) : ViewModel() {

    private val _children = MutableStateFlow<List<FirebaseStudent>>(emptyList())
    val children: StateFlow<List<FirebaseStudent>> = _children

    private val _observations = MutableStateFlow<List<Observacion>>(emptyList())
    val observations: StateFlow<List<Observacion>> = _observations

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadParentData(parentEmail: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val childList = firebaseRepository.obtenerEstudiantesPorEmailPadre(parentEmail)
            _children.value = childList
            
            val allObs = mutableListOf<Observacion>()
            childList.forEach { child ->
                val obs = firebaseRepository.obtenerObservaciones(child.pin.toString())
                allObs.addAll(obs)
            }
            _observations.value = allObs
            _isLoading.value = false
        }
    }

    fun requestJoinClassroom(
        joinCode: String,
        studentId: String,
        studentName: String,
        parentId: String,
        parentEmail: String,
        onResult: (JoinRequestResult) -> Unit
    ) {
        viewModelScope.launch {
            val result = firebaseRepository.solicitarUnirseASalom(
                joinCode = joinCode,
                studentId = studentId,
                studentName = studentName,
                parentId = parentId,
                parentEmail = parentEmail
            )
            onResult(result)
        }
    }

    fun createOrResetChildPin(studentId: String, newPin: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = firebaseRepository.crearCredencialEstudiante(studentId, newPin)
            onResult(success)
        }
    }
}
