package com.josuefp.servicenotebook.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.josuefp.servicenotebook.data.local.entity.ProductEntity
import com.josuefp.servicenotebook.data.local.entity.ServiceEntity
import com.josuefp.servicenotebook.data.local.model.ServiceMaterialWithProduct
import com.josuefp.servicenotebook.data.repository.CatalogRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CatalogViewModel(private val repository: CatalogRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedServiceId = MutableStateFlow<Long?>(null)
    val selectedServiceId: StateFlow<Long?> = _selectedServiceId

    val services: StateFlow<List<ServiceEntity>> = repository.getAllActiveServices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProducts: StateFlow<List<ProductEntity>> = repository.getAllProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredServices: StateFlow<List<ServiceEntity>> = combine(services, _searchQuery) { list, query ->
        if (query.isBlank()) list
        else list.filter { it.name.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedServiceMaterials: StateFlow<List<ServiceMaterialWithProduct>> = _selectedServiceId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else repository.getServiceMaterialsWithProducts(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectService(serviceId: Long?) {
        _selectedServiceId.value = serviceId
    }

    fun saveService(
        service: ServiceEntity,
        materials: List<Pair<Long, Double>>
    ) {
        viewModelScope.launch {
            repository.saveServiceWithMaterials(service, materials)
        }
    }

    fun deleteService(service: ServiceEntity) {
        viewModelScope.launch {
            repository.deleteService(service)
        }
    }

    fun calculateSuggestedPrice(
        materialsCost: Double,
        laborCost: Double,
        overheadCost: Double,
        profitMarginPercent: Double
    ): Double {
        val totalCost = materialsCost + laborCost + overheadCost
        return totalCost * (1.0 + (profitMarginPercent / 100.0))
    }
}

class CatalogViewModelFactory(private val repository: CatalogRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CatalogViewModel::class.java)) {
            return CatalogViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
