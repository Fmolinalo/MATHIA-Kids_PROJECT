package com.example.mathia.viewModels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

enum class ThemeMode {
    LIGHT
}

@HiltViewModel
class ThemeViewModel @Inject constructor() : ViewModel() {
    private val _themeMode = MutableStateFlow(ThemeMode.LIGHT)
    val themeMode: StateFlow<ThemeMode> = _themeMode

    fun cambiarTema(nuevoModo: ThemeMode = ThemeMode.LIGHT) {
        _themeMode.value = ThemeMode.LIGHT
    }
}