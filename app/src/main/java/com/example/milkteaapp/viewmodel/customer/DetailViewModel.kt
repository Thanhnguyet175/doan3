package com.example.milkteaapp.viewmodel.customer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.milkteaapp.model.data.*
import com.example.milkteaapp.model.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class DetailUiState(
    val isLoading: Boolean = false,
    val product: Product? = null,
    val availableToppings: List<Topping> = emptyList(),

    // Các lựa chọn hiện tại của user
    val selectedSize: DrinkSize = DrinkSize.MEDIUM,
    val selectedSugar: SugarLevel = SugarLevel.FULL,
    val selectedIce: IceLevel = IceLevel.NORMAL,
    val selectedToppings: List<Topping> = emptyList(),
    val quantity: Int = 1,
    val note: String = "",

    val addedToCart: Boolean = false,   // trigger snackbar "Đã thêm vào giỏ"
    val errorMessage: String? = null
) {
    /** Giá hiển thị = giá theo size + tổng topping */
    val currentUnitPrice: Long
        get() {
            val sizePrice = product?.priceForSize(selectedSize) ?: 0L
            val toppingPrice = selectedToppings.sumOf { it.price }
            return sizePrice + toppingPrice
        }

    val totalPrice: Long get() = currentUnitPrice * quantity
}

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val productRepository: ProductRepository
) : ViewModel() {

    // productId được truyền qua Navigation argument
    private val productId: String = checkNotNull(savedStateHandle["productId"])

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init { loadProduct() }

    fun loadProduct() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val productResult = productRepository.getProductById(productId)
            productResult.fold(
                onSuccess = { product ->
                    // Tải topping song song
                    val toppings = productRepository
                        .getToppingsByIds(product.availableToppings)
                        .getOrDefault(emptyList())

                    _uiState.update {
                        it.copy(
                            isLoading         = false,
                            product           = product,
                            availableToppings = toppings,
                            // Default options theo sản phẩm
                            selectedSize      = if (DrinkSize.MEDIUM in product.sizePrices.keys)
                                DrinkSize.MEDIUM else
                                product.sizePrices.keys.firstOrNull() ?: DrinkSize.MEDIUM,
                            selectedSugar     = product.sugarOptions.lastOrNull() ?: SugarLevel.FULL,
                            selectedIce       = product.iceOptions.getOrElse(1) { IceLevel.NORMAL }
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
            )
        }
    }

    // ── Các lựa chọn ─────────────────────────────────────────────────────────

    fun selectSize(size: DrinkSize)     = _uiState.update { it.copy(selectedSize = size) }
    fun selectSugar(sugar: SugarLevel)  = _uiState.update { it.copy(selectedSugar = sugar) }
    fun selectIce(ice: IceLevel)        = _uiState.update { it.copy(selectedIce = ice) }
    fun onNoteChange(note: String)      = _uiState.update { it.copy(note = note) }

    fun toggleTopping(topping: Topping) {
        _uiState.update { state ->
            val current = state.selectedToppings.toMutableList()
            if (current.any { it.id == topping.id }) current.removeAll { it.id == topping.id }
            else current.add(topping)
            state.copy(selectedToppings = current)
        }
    }

    fun increaseQty() = _uiState.update { it.copy(quantity = it.quantity + 1) }
    fun decreaseQty() = _uiState.update { it.copy(quantity = (it.quantity - 1).coerceAtLeast(1)) }

    // ── Thêm vào giỏ ─────────────────────────────────────────────────────────

    /**
     * Tạo [CartItem] từ lựa chọn hiện tại và trả về để CartViewModel xử lý.
     * Đồng thời emit [DetailUiState.addedToCart] = true để hiển thị snackbar.
     */
    fun buildCartItem(): CartItem? {
        val state = _uiState.value
        val product = state.product ?: return null
        return CartItem(
            cartItemId        = UUID.randomUUID().toString(),
            productId         = product.id,
            productName       = product.name,
            productImageUrl   = product.imageUrl,
            size              = state.selectedSize,
            sugarLevel        = state.selectedSugar,
            iceLevel          = state.selectedIce,
            selectedToppings  = state.selectedToppings,
            unitPrice         = state.currentUnitPrice,
            quantity          = state.quantity,
            note              = state.note
        )
    }

    fun onAddedToCart() = _uiState.update { it.copy(addedToCart = true) }
    fun onAddedToCartHandled() = _uiState.update { it.copy(addedToCart = false) }
    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
}