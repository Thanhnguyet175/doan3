package com.example.milkteaapp.viewmodel.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.milkteaapp.model.data.Category
import com.example.milkteaapp.model.data.Product
import com.example.milkteaapp.model.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MenuUiState(
    val isLoading: Boolean = false,
    val categories: List<Category> = emptyList(),
    val allProducts: List<Product> = emptyList(),
    val filteredProducts: List<Product> = emptyList(),
    val selectedCategoryId: String? = null,   // null = "Tất cả"
    val searchQuery: String = "",
    val errorMessage: String? = null
)

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MenuUiState())
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    init { loadMenu() }

    fun loadMenu() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val categoriesDeferred = async { productRepository.getCategories() }
            val productsDeferred   = async { productRepository.getAvailableProducts() }

            val categories = categoriesDeferred.await().getOrDefault(emptyList())
            val products   = productsDeferred.await().getOrDefault(emptyList())

            _uiState.update {
                it.copy(
                    isLoading        = false,
                    categories       = categories,
                    allProducts      = products,
                    filteredProducts = products   // ban đầu hiển thị tất cả
                )
            }
        }
    }

    /** 🟢 SỬA LẠI: Lọc danh mục đồng bộ trạng thái tìm kiếm */
    fun selectCategory(categoryId: String?) {
        _uiState.update {
            val updatedState = it.copy(selectedCategoryId = categoryId)
            // Ép cập nhật bộ lọc mới dựa trên danh mục vừa đổi
            updatedState.copy(filteredProducts = applyFilterLogic(updatedState))
        }
    }

    /** Tìm kiếm theo tên sản phẩm */
    fun onSearchQueryChange(query: String) {
        _uiState.update {
            val updatedState = it.copy(searchQuery = query)
            updatedState.copy(filteredProducts = applyFilterLogic(updatedState))
        }
    }

    fun clearSearch() {
        _uiState.update {
            val updatedState = it.copy(searchQuery = "")
            updatedState.copy(filteredProducts = applyFilterLogic(updatedState))
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }

    // ── 🟢 SỬA LẠI: Tách hàm xử lý logic thuần túy tránh xung đột State luồng ──

    private fun applyFilterLogic(state: MenuUiState): List<Product> {
        return state.allProducts.filter { product ->
            val matchCategory = state.selectedCategoryId == null ||
                    product.categoryId == state.selectedCategoryId
            val matchSearch = state.searchQuery.isBlank() ||
                    product.name.contains(state.searchQuery.trim(), ignoreCase = true)
            matchCategory && matchSearch
        }
    }
}