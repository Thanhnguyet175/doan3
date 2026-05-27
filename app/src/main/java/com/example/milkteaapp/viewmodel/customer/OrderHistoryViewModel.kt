package com.example.milkteaapp.viewmodel.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.milkteaapp.model.data.Order
import com.example.milkteaapp.model.data.OrderStatus
import com.example.milkteaapp.model.repository.AuthRepository
import com.example.milkteaapp.model.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrderHistoryUiState(
    val isLoading: Boolean = false,
    val allOrders: List<Order> = emptyList(),
    val filteredOrders: List<Order> = emptyList(),
    val selectedStatus: OrderStatus? = null,   // null = tất cả
    val selectedOrder: Order? = null,           // đơn đang xem chi tiết
    val cancelSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class OrderHistoryViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderHistoryUiState())
    val uiState: StateFlow<OrderHistoryUiState> = _uiState.asStateFlow()

    init { loadOrders() }

    fun loadOrders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val uid = authRepository.currentUid
            if (uid == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Chưa đăng nhập.") }
                return@launch
            }
            try {
                // 🟢 ĐÃ FIX: Chuyển sang dùng hàm lắng nghe Realtime cực xịn của Repository
                orderRepository.observeCustomerOrders(uid).collect { orders ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            allOrders = orders,
                            filteredOrders = applyStatusFilter(orders, it.selectedStatus)
                        )
                    }
                }
            } catch (e: Exception) {
                // Bắt lỗi nếu mạng rớt hoặc Firebase có vấn đề
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    // ── Lọc theo trạng thái ───────────────────────────────────────────────────

    fun filterByStatus(status: OrderStatus?) {
        _uiState.update { state ->
            state.copy(
                selectedStatus = status,
                filteredOrders = applyStatusFilter(state.allOrders, status)
            )
        }
    }

    // ── Xem chi tiết ──────────────────────────────────────────────────────────

    fun selectOrder(order: Order)   = _uiState.update { it.copy(selectedOrder = order) }
    fun clearSelectedOrder()        = _uiState.update { it.copy(selectedOrder = null) }

    // ── Huỷ đơn ──────────────────────────────────────────────────────────────

    /**
     * Chỉ cho phép huỷ khi đơn đang ở trạng thái PENDING.
     */
    fun cancelOrder(orderId: String) {
        val order = _uiState.value.allOrders.find { it.id == orderId }
        if (order?.status != OrderStatus.PENDING) {
            _uiState.update { it.copy(errorMessage = "Chỉ có thể huỷ đơn đang chờ xác nhận.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            orderRepository.cancelOrder(orderId).fold(
                onSuccess = {
                    // Cập nhật local list ngay, không cần reload toàn bộ
                    _uiState.update { state ->
                        val updated = state.allOrders.map { o ->
                            if (o.id == orderId) o.copy(status = OrderStatus.CANCELLED) else o
                        }
                        state.copy(
                            isLoading      = false,
                            cancelSuccess  = true,
                            allOrders      = updated,
                            filteredOrders = applyStatusFilter(updated, state.selectedStatus)
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
            )
        }
    }

    // ── Reset ─────────────────────────────────────────────────────────────────

    fun onCancelHandled() = _uiState.update { it.copy(cancelSuccess = false) }
    fun clearError()      = _uiState.update { it.copy(errorMessage = null) }

    // ── Helper ────────────────────────────────────────────────────────────────

    private fun applyStatusFilter(orders: List<Order>, status: OrderStatus?): List<Order> =
        if (status == null) orders else orders.filter { it.status == status }
}