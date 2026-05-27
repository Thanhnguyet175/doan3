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
            // CHỈ LẤY ĐƠN HÀNG HOÀN THÀNH (COMPLETED) ĐỂ TÍNH DOANH THU & SẢN PHẨM BÁN CHẠY
            val completedOrdersDeferred = async { orderRepository.getOrdersByStatus(OrderStatus.COMPLETED) }

            val categories    = categoriesDeferred.await().getOrDefault(emptyList())
            val featured      = featuredDeferred.await().getOrDefault(emptyList())
            val customerName  = userDeferred.await().getOrNull()?.fullName ?: ""
            val completedOrders = completedOrdersDeferred.await().getOrDefault(emptyList())

            // 2. LOGIC LỌC ĐỘNG: Duyệt qua các đơn hàng hoàn thành để cộng dồn số lượng bán ra
            val productSalesMap = mutableMapOf<String, Int>()

            for (order in completedOrders) {
                order.items?.forEach { item ->
                    val pId = item.productId
                    val qty = item.quantity
                    if (!pId.isNullOrBlank()) {
                        val currentQty = productSalesMap[pId] ?: 0
                        productSalesMap[pId] = currentQty + qty
                    }
                }
            }

            // Lấy danh sách ID của 5 sản phẩm có tổng lượt mua cao nhất từ các đơn hàng thành công
            val top5ProductIds = productSalesMap.entries
                .sortedByDescending { it.value }
                .take(5)
                .map { it.key }

            // Đối chiếu ID lấy thông tin chi tiết sản phẩm và sắp xếp theo số lượng bán từ cao đến thấp
            val dynamicBestSellers = featured.filter { product ->
                product.id in top5ProductIds
            }.distinctBy { product ->
                product.id
            }.sortedByDescending { product ->
                productSalesMap[product.id] ?: 0
            }

            // 3. Đổ dữ liệu sạch vào UI State để Composable cập nhật màn hình công khai
            _uiState.update {
                it.copy(
                    isLoading        = false,
                    customerName     = customerName,
                    categories       = categories,
                    bestSellers      = dynamicBestSellers, // Trả danh sách thực tế đã sắp xếp ra view
                    featuredProducts = featured
                )
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
}