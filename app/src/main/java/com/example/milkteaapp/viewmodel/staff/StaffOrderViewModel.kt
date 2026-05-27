package com.example.milkteaapp.viewmodel.staff

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

// ── UI State ──────────────────────────────────────────────────────────────────

data class StaffOrderUiState(
    val isLoading: Boolean = false,

    /** Danh sách đơn đang hoạt động (realtime Flow từ Firestore) */
    val activeOrders: List<Order> = emptyList(),

    /** Tab đang được chọn trên StaffDashboard */
    val selectedTab: StaffTab = StaffTab.IN_PROGRESS,

    /** Đơn được chọn để xem chi tiết / thao tác */
    val selectedOrder: Order? = null,

    /** ID đơn đang được cập nhật trạng thái (hiển thị loading per-card) */
    val updatingOrderId: String? = null,

    val successMessage: String? = null,
    val errorMessage: String? = null
) {
    /** Lọc đơn theo tab hiện tại */
    val displayedOrders: List<Order>
        get() = when (selectedTab) {
            StaffTab.IN_PROGRESS -> activeOrders.filter {
                it.status in listOf(
                    OrderStatus.PENDING,
                    OrderStatus.CONFIRMED,
                    OrderStatus.BREWING,
                    OrderStatus.DELAYED
                )
            }
            StaffTab.READY -> activeOrders.filter { it.status == OrderStatus.READY }
        }

    /** Số đơn đang chờ xử lý – dùng cho badge trên tab */
    val pendingCount: Int
        get() = activeOrders.count { it.status == OrderStatus.PENDING }
}

enum class StaffTab(val label: String) {
    IN_PROGRESS("Đang xử lý"),
    READY("Sẵn sàng")
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class StaffOrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StaffOrderUiState())
    val uiState: StateFlow<StaffOrderUiState> = _uiState.asStateFlow()

    init { observeActiveOrders() }

    // ── Realtime listener ────────────────────────────────────────────────────

    /**
     * Lắng nghe Firestore realtime – tự động cập nhật khi có đơn mới
     * hoặc khi đơn đổi trạng thái (kể cả từ thiết bị khác).
     */
    private fun observeActiveOrders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            orderRepository.observeActiveOrders().collect { orders ->
                _uiState.update {
                    it.copy(isLoading = false, activeOrders = orders)
                }
            }
        }
    }

    // ── Điều hướng tab ───────────────────────────────────────────────────────

    fun selectTab(tab: StaffTab) = _uiState.update { it.copy(selectedTab = tab) }

    // ── Xem chi tiết ─────────────────────────────────────────────────────────

    fun selectOrder(order: Order) = _uiState.update { it.copy(selectedOrder = order) }
    fun clearSelectedOrder()      = _uiState.update { it.copy(selectedOrder = null) }

    // ── Cập nhật trạng thái ──────────────────────────────────────────────────

    /**
     * Bước tiếp theo trong vòng đời đơn hàng:
     *   PENDING → CONFIRMED → BREWING → READY → COMPLETED
     *
     * Nhân viên chỉ được dùng hàm này (không nhảy cóc trạng thái).
     */
    fun processNextStep(orderId: String) {
        val order = findOrder(orderId) ?: return
        val nextStatus = order.status.nextStaffStep() ?: run {
            _uiState.update { it.copy(errorMessage = "Đơn hàng đã ở trạng thái cuối.") }
            return
        }
        updateStatus(orderId, nextStatus)
    }

    /**
     * Đánh dấu đơn bị trễ (có thể từ bất kỳ trạng thái đang xử lý nào).
     */
    fun markDelayed(orderId: String) {
        val order = findOrder(orderId) ?: return
        if (order.status !in listOf(OrderStatus.CONFIRMED, OrderStatus.BREWING)) {
            _uiState.update { it.copy(errorMessage = "Chỉ đánh dấu trễ khi đang pha chế hoặc đã xác nhận.") }
            return
        }
        updateStatus(orderId, OrderStatus.DELAYED)
    }

    /**
     * Tiếp tục xử lý đơn bị đánh dấu trễ → quay về BREWING.
     */
    fun resumeDelayed(orderId: String) {
        val order = findOrder(orderId) ?: return
        if (order.status != OrderStatus.DELAYED) {
            _uiState.update { it.copy(errorMessage = "Đơn hàng không ở trạng thái trễ.") }
            return
        }
        updateStatus(orderId, OrderStatus.BREWING)
    }

    /**
     * Gán bản thân (nhân viên đang đăng nhập) làm người xử lý đơn.
     */
    fun assignSelf(orderId: String) {
        val staffId = authRepository.currentUid ?: return
        viewModelScope.launch {
            orderRepository.assignStaff(orderId, staffId).fold(
                onSuccess = {
                    _uiState.update { it.copy(successMessage = "Đã nhận đơn #$orderId.") }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(errorMessage = e.message) }
                }
            )
        }
    }

    // ── Reset messages ────────────────────────────────────────────────────────

    fun clearSuccess() = _uiState.update { it.copy(successMessage = null) }
    fun clearError()   = _uiState.update { it.copy(errorMessage = null) }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun updateStatus(orderId: String, status: OrderStatus) {
        viewModelScope.launch {
            _uiState.update { it.copy(updatingOrderId = orderId) }
            orderRepository.updateStatus(orderId, status).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            updatingOrderId = null,
                            successMessage  = "Cập nhật trạng thái: ${status.label}"
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(updatingOrderId = null, errorMessage = e.message) }
                }
            )
        }
    }

    private fun findOrder(orderId: String): Order? =
        _uiState.value.activeOrders.find { it.id == orderId }
            ?: run { _uiState.update { it.copy(errorMessage = "Không tìm thấy đơn hàng.") }; null }
}

// ── Extension: bước tiếp theo hợp lệ của nhân viên ───────────────────────────

private fun OrderStatus.nextStaffStep(): OrderStatus? = when (this) {
    OrderStatus.PENDING   -> OrderStatus.CONFIRMED
    OrderStatus.CONFIRMED -> OrderStatus.BREWING
    OrderStatus.BREWING   -> OrderStatus.READY
    OrderStatus.DELAYED   -> OrderStatus.BREWING   // tiếp tục sau khi trễ
    OrderStatus.READY     -> OrderStatus.COMPLETED
    else                  -> null                  // COMPLETED / CANCELLED → không có bước tiếp
}