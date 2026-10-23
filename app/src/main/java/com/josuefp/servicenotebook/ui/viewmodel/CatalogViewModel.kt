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

data class QuoteItem(
    val service: ServiceEntity,
    val quantity: Int = 1
)

data class LiveQuoteState(
    val items: List<QuoteItem> = emptyList(),
    val discount: Double = 0.0,
    val notes: String = ""
) {
    val subtotal: Double
        get() = items.sumOf { it.service.basePrice * it.quantity }

    val totalDurationMinutes: Int
        get() = items.sumOf { it.service.estimatedDurationMinutes * it.quantity }

    val total: Double
        get() = (subtotal - discount).coerceAtLeast(0.0)
}

enum class CatalogTab {
    SERVICES_LIST,
    LIVE_CALCULATOR
}

enum class CatalogSortOrder(val label: String) {
    ALPHABETICAL("A-Z"),
    PRICE_ASC("Precio: menor a mayor"),
    PRICE_DESC("Precio: mayor a menor"),
    DATE_NEWEST("Más recientes"),
    DATE_OLDEST("Más antiguos")
}

class CatalogViewModel(private val repository: CatalogRepository) : ViewModel() {

    private val _selectedTab = MutableStateFlow(CatalogTab.SERVICES_LIST)
    val selectedTab: StateFlow<CatalogTab> = _selectedTab

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategory = MutableStateFlow("Todos")
    val selectedCategory: StateFlow<String> = _selectedCategory

    private val _sortOrder = MutableStateFlow(CatalogSortOrder.ALPHABETICAL)
    val sortOrder: StateFlow<CatalogSortOrder> = _sortOrder

    private val _selectedServiceId = MutableStateFlow<Long?>(null)
    val selectedServiceId: StateFlow<Long?> = _selectedServiceId

    private val _quoteState = MutableStateFlow(LiveQuoteState())
    val quoteState: StateFlow<LiveQuoteState> = _quoteState

    val services: StateFlow<List<ServiceEntity>> = repository.getAllActiveServices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProducts: StateFlow<List<ProductEntity>> = repository.getAllProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredServices: StateFlow<List<ServiceEntity>> = combine(
        services,
        _searchQuery,
        _selectedCategory,
        _sortOrder
    ) { list, query, category, sort ->
        var result = list

        if (category != "Todos") {
            result = result.filter { it.category.equals(category, ignoreCase = true) }
        }

        if (query.isNotBlank()) {
            result = result.filter {
                it.name.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true)
            }
        }

        when (sort) {
            CatalogSortOrder.ALPHABETICAL -> result.sortedBy { it.name.lowercase() }
            CatalogSortOrder.PRICE_ASC -> result.sortedBy { it.basePrice }
            CatalogSortOrder.PRICE_DESC -> result.sortedByDescending { it.basePrice }
            CatalogSortOrder.DATE_NEWEST -> result.sortedByDescending { it.createdAt }
            CatalogSortOrder.DATE_OLDEST -> result.sortedBy { it.createdAt }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedServiceMaterials: StateFlow<List<ServiceMaterialWithProduct>> = _selectedServiceId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else repository.getServiceMaterialsWithProducts(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSelectedTab(tab: CatalogTab) {
        _selectedTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSortOrder(order: CatalogSortOrder) {
        _sortOrder.value = order
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

    // Live Quote Calculator Actions
    fun addServiceToQuote(service: ServiceEntity) {
        val currentItems = _quoteState.value.items.toMutableList()
        val existingIndex = currentItems.indexOfFirst { it.service.id == service.id }
        if (existingIndex >= 0) {
            val existing = currentItems[existingIndex]
            currentItems[existingIndex] = existing.copy(quantity = existing.quantity + 1)
        } else {
            currentItems.add(QuoteItem(service = service, quantity = 1))
        }
        _quoteState.value = _quoteState.value.copy(items = currentItems)
    }

    fun updateQuoteItemQuantity(serviceId: Long, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeServiceFromQuote(serviceId)
            return
        }
        val currentItems = _quoteState.value.items.toMutableList()
        val index = currentItems.indexOfFirst { it.service.id == serviceId }
        if (index >= 0) {
            currentItems[index] = currentItems[index].copy(quantity = newQuantity)
            _quoteState.value = _quoteState.value.copy(items = currentItems)
        }
    }

    fun removeServiceFromQuote(serviceId: Long) {
        val currentItems = _quoteState.value.items.filterNot { it.service.id == serviceId }
        _quoteState.value = _quoteState.value.copy(items = currentItems)
    }

    fun setQuoteDiscount(discount: Double) {
        _quoteState.value = _quoteState.value.copy(discount = discount.coerceAtLeast(0.0))
    }

    fun clearQuote() {
        _quoteState.value = LiveQuoteState()
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
