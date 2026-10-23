package com.josuefp.servicenotebook.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.josuefp.servicenotebook.data.local.entity.WorkPhotoEntity
import com.josuefp.servicenotebook.data.repository.GalleryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class GalleryTab {
    HANDS,
    FEET
}

class GalleryViewModel(private val repository: GalleryRepository) : ViewModel() {

    private val _selectedTab = MutableStateFlow(GalleryTab.HANDS)
    val selectedTab: StateFlow<GalleryTab> = _selectedTab

    @OptIn(ExperimentalCoroutinesApi::class)
    val photos: StateFlow<List<WorkPhotoEntity>> = _selectedTab
        .flatMapLatest { tab ->
            val category = if (tab == GalleryTab.HANDS) "Manos" else "Pies"
            repository.getPhotosByCategory(category)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSelectedTab(tab: GalleryTab) {
        _selectedTab.value = tab
    }

    fun addPhoto(context: Context, uri: Uri, category: String, description: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.savePhotoFromUri(context, uri, category, description)
            onSuccess()
        }
    }

    fun deletePhoto(context: Context, photo: WorkPhotoEntity) {
        viewModelScope.launch {
            repository.deletePhoto(context, photo)
        }
    }
}

class GalleryViewModelFactory(private val repository: GalleryRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GalleryViewModel::class.java)) {
            return GalleryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
