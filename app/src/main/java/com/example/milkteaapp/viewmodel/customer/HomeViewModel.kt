package com.example.milkteaapp.viewmodel.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.milkteaapp.model.data.Category
import com.example.milkteaapp.model.data.Product
import com.example.milkteaapp.model.data.Promotion
import com.example.milkteaapp.model.data.OrderStatus
import com.example.milkteaapp.model.repository.AuthRepository
import com.example.milkteaapp.model.repository.ProductRepository
import com.example.milkteaapp.model.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val customerName: String = "",
    val bannerPromotions: List<Promotion> = emptyList(),
    val categories: List<Category> = emptyList(),
    val bestSellers: List<Product> = emptyList(),
    val featuredProducts: List<Product> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val authRepository: AuthRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init { loadHomeData() }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // 1. Tải song song dữ liệu gốc từ các Repository
            val categoriesDeferred    = async { productRepository.getCategories() }
            val featuredDeferred      = async { productRepository.getFeaturedProducts() }
            val userDeferred          = async { authRepository.getCurrentUser() }
            val completedOrdersDeferred = async { orderRepository.getOrdersByStatus(OrderStatus.COMPLETED) }

            val categories    = categoriesDeferred.await().getOrDefault(emptyList())
            val featured      = featuredDeferred.await().getOrDefault(emptyList())
            val customerName  = userDeferred.await().getOrNull()?.fullName ?: ""
            val completedOrders = completedOrdersDeferred.await().getOrDefault(emptyList())

            // 2. LOGIC LỌC ĐỘNG: Đếm số lượng sản phẩm từ các đơn hàng thành công
            val productSalesMap = mutableMapOf<String, Int>()

            for (order in completedOrders) {
                // Sử dụng toán tử điều hướng an toàn tránh crash nếu items rỗng
                order.items?.forEach { item ->
                    val pId = item.productId
                    val qty = item.quantity
                    if (!pId.isNullOrBlank()) {
                        val currentQty = productSalesMap[pId] ?: 0
                        productSalesMap[pId] = currentQty + qty
                    }
                }
            }

            // Lấy danh sách ID của 3 sản phẩm có tổng lượt mua cao nhất từ các đơn hàng đã hoàn thành
            val top3ProductIds = productSalesMap.entries
                .sortedByDescending { it.value }
                .take(3)
                .map { it.key }

            // 🟢 ĐÃ FIX: Sử dụng danh sách featured (hoặc danh sách sản phẩm tổng của hệ thống) để map thông tin chi tiết chi tiết
            val dynamicBestSellers = featured.filter { product ->
                product.id in top3ProductIds
            }.distinctBy { product ->
                product.id
            }.sortedByDescending { product ->
                productSalesMap[product.id] ?: 0
            }

            // 3. Cập nhật dữ liệu sạch vào UI State
            _uiState.update {
                it.copy(
                    isLoading        = false,
                    customerName     = customerName,
                    categories       = categories,
                    bestSellers      = dynamicBestSellers, // Đổ đúng 3 món mua nhiều nhất vào đây
                    featuredProducts = featured
                )
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
}