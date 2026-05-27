package com.example.milkteaapp.viewmodel.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.milkteaapp.model.data.*
import com.example.milkteaapp.model.repository.AuthRepository
import com.example.milkteaapp.model.repository.OrderRepository
import com.example.milkteaapp.model.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val appliedPromotion: Promotion? = null,
    val discountAmount: Long = 0L,
    val paymentMethod: String = "CASH",
    val deliveryAddress: String = "",
    val orderNote: String = "",
    val isLoading: Boolean = false,
    val orderPlaced: Boolean = false,      // trigger điều hướng sang OrderHistory
    val errorMessage: String? = null,
    val promoErrorMessage: String? = null
) {
    val subtotal: Long   get() = items.totalAmount()
    val finalAmount: Long get() = (subtotal - discountAmount).coerceAtLeast(0L)
    val itemCount: Int   get() = items.totalQuantity()
    val isEmpty: Boolean get() = items.isEmpty()
}

@HiltViewModel
class CartViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    // ── Thêm / xoá / cập nhật ────────────────────────────────────────────────

    /** Thêm CartItem (gọi từ DetailViewModel) */
    fun addItem(item: CartItem) {
        _uiState.update { state ->
            // Nếu cùng productId + cùng options → tăng qty thay vì tạo dòng mới
            val existing = state.items.indexOfFirst { it.isSameOptionAs(item) }
            val newItems = if (existing >= 0) {
                state.items.toMutableList().also {
                    it[existing] = it[existing].withQuantity(it[existing].quantity + item.quantity)
                }
            } else {
                state.items + item
            }
            state.copy(items = newItems)
        }
    }

    fun removeItem(cartItemId: String) {
        _uiState.update { it.copy(items = it.items.filter { i -> i.cartItemId != cartItemId }) }
        revalidatePromotion()
    }

    fun increaseQty(cartItemId: String) = updateQty(cartItemId, +1)
    fun decreaseQty(cartItemId: String) = updateQty(cartItemId, -1)

    private fun updateQty(cartItemId: String, delta: Int) {
        _uiState.update { state ->
            val newItems = state.items.mapNotNull { item ->
                if (item.cartItemId != cartItemId) item
                else {
                    val newQty = item.quantity + delta
                    if (newQty <= 0) null else item.withQuantity(newQty)
                }
            }
            state.copy(items = newItems)
        }
        revalidatePromotion()
    }

    fun clearCart() = _uiState.update {
        it.copy(items = emptyList(), appliedPromotion = null, discountAmount = 0L)
    }

    // ── Khuyến mãi ───────────────────────────────────────────────────────────

    /** Áp dụng khuyến mãi – validate trước khi lưu */
    fun applyPromotion(promotion: Promotion) {
        val state = _uiState.value
        if (!promotion.isValid()) {
            _uiState.update { it.copy(promoErrorMessage = "Mã khuyến mãi đã hết hạn hoặc không hợp lệ.") }
            return
        }
        val discount = promotion.calculateDiscount(state.subtotal)
        if (discount <= 0) {
            _uiState.update { it.copy(promoErrorMessage = "Đơn hàng chưa đủ điều kiện áp dụng mã này.") }
            return
        }
        _uiState.update {
            it.copy(appliedPromotion = promotion, discountAmount = discount, promoErrorMessage = null)
        }
    }

    fun removePromotion() = _uiState.update {
        it.copy(appliedPromotion = null, discountAmount = 0L, promoErrorMessage = null)
    }

    // ── Thông tin đặt hàng ────────────────────────────────────────────────────

    fun setPaymentMethod(method: String)    = _uiState.update { it.copy(paymentMethod = method) }
    fun setDeliveryAddress(address: String) = _uiState.update { it.copy(deliveryAddress = address) }
    fun setOrderNote(note: String)          = _uiState.update { it.copy(orderNote = note) }

    // ── Đặt hàng ─────────────────────────────────────────────────────────────

    fun placeOrder() {
        val state = _uiState.value
        if (state.isEmpty) {
            _uiState.update { it.copy(errorMessage = "Giỏ hàng trống.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val userResult = authRepository.getCurrentUser()
            val user = userResult.getOrElse {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Vui lòng đăng nhập lại.") }
                return@launch
            }
            orderRepository.placeOrder(
                customer        = user,
                cartItems       = state.items,
                promotionId     = state.appliedPromotion?.id,
                discountAmount  = state.discountAmount,
                paymentMethod   = state.paymentMethod,
                deliveryAddress = state.deliveryAddress.ifBlank { null },
                note            = state.orderNote
            ).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(isLoading = false, orderPlaced = true,
                            items = emptyList(), appliedPromotion = null, discountAmount = 0L)
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
            )
        }
    }

    // ── Reset ─────────────────────────────────────────────────────────────────

    fun onOrderPlacedHandled() = _uiState.update { it.copy(orderPlaced = false) }
    fun clearError()           = _uiState.update { it.copy(errorMessage = null) }
    fun clearPromoError()      = _uiState.update { it.copy(promoErrorMessage = null) }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun revalidatePromotion() {
        val state = _uiState.value
        val promo = state.appliedPromotion ?: return
        val newDiscount = promo.calculateDiscount(state.subtotal)
        if (newDiscount <= 0) removePromotion()
        else _uiState.update { it.copy(discountAmount = newDiscount) }
    }
}

/** Kiểm tra 2 CartItem có cùng sản phẩm + options không (để merge) */
private fun CartItem.isSameOptionAs(other: CartItem): Boolean =
    productId == other.productId &&
            size == other.size &&
            sugarLevel == other.sugarLevel &&
            iceLevel == other.iceLevel &&
            selectedToppings.map { it.id }.sorted() == other.selectedToppings.map { it.id }.sorted() &&
            note == other.note