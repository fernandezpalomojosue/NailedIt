package com.josuefp.servicenotebook.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.josuefp.servicenotebook.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {

    private val _appName = MutableStateFlow("Virna Nails")
    val appName: StateFlow<String> = _appName

    private val _themePalette = MutableStateFlow("ROSE")
    val themePalette: StateFlow<String> = _themePalette

    init {
        viewModelScope.launch {
            val config = repository.getAppConfigOnce()
            _appName.value = config.appName
            _themePalette.value = config.themePalette
        }
    }

    fun updateAppName(newName: String) {
        val trimmed = newName.trim().ifBlank { "Virna Nails" }
        _appName.value = trimmed
        viewModelScope.launch {
            repository.updateAppName(trimmed)
        }
    }

    fun updateThemePalette(newPalette: String) {
        _themePalette.value = newPalette
        viewModelScope.launch {
            repository.updateThemePalette(newPalette)
        }
    }
}

class SettingsViewModelFactory(private val repository: SettingsRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
