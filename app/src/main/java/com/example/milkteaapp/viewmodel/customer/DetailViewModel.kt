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

    val selectedSize: DrinkSize = DrinkSize.MEDIUM,
    val selectedSugar: SugarLevel = SugarLevel.ONE_HUNDRED,

    // Quản lý 2 trạng thái đá riêng biệt tại tầng Logic
    val selectedIcePack: IcePackOption = IcePackOption.DA_CHUNG,
    val selectedIceLevel: IceLevel = IceLevel.NORMAL,

    val selectedToppings: List<Topping> = emptyList(),
    val quantity: Int = 1,
    val note: String = "",

    val addedToCart: Boolean = false,
    val errorMessage: String? = null
) {
    val currentUnitPrice: Long
        get() {
            val prod = product ?: return 0L
            val priceForSize = prod.sizePrices[selectedSize] ?: prod.basePrice
            val toppingSum = selectedToppings.sumOf { it.price }
            return priceForSize + toppingSum
        }

    val totalPrice: Long
        get() = currentUnitPrice * quantity
}

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private val productId: String? = savedStateHandle["productId"]

    init {
        loadProductDetail()
    }

    private fun loadProductDetail() {
        val pId = productId
        if (pId.isNullOrBlank()) {
            _uiState.update { it.copy(errorMessage = "Không tìm thấy mã sản phẩm.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val productDeferred = async { productRepository.getProductById(pId) }
                val toppingDeferred = async { productRepository.getAllToppings() }

                val product = productDeferred.await().getOrNull()
                val allToppings = toppingDeferred.await().getOrDefault(emptyList())

                if (product != null) {
                    val allowedToppings = allToppings.filter { t ->
                        t.isAvailable && product.toppingIds.contains(t.id)
                    }

                    _uiState.update { state ->
                        state.copy(
                            isLoading         = false,
                            product           = product,
                            availableToppings = allowedToppings,
                            selectedSize      = product.availableSizes.firstOrNull() ?: DrinkSize.MEDIUM,
                            selectedSugar     = product.sugarOptions.firstOrNull() ?: SugarLevel.ONE_HUNDRED,
                            selectedIcePack   = IcePackOption.DA_CHUNG,
                            selectedIceLevel  = product.iceOptions.firstOrNull() ?: IceLevel.NORMAL
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Sản phẩm không tồn tại.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.localizedMessage ?: "Lỗi tải dữ liệu.") }
            }
        }
    }

    fun selectSize(size: DrinkSize) = _uiState.update { it.copy(selectedSize = size) }
    fun selectSugar(sugar: SugarLevel) = _uiState.update { it.copy(selectedSugar = sugar) }
    fun selectIcePack(option: IcePackOption) = _uiState.update { it.copy(selectedIcePack = option) }
    fun selectIceLevel(level: IceLevel) = _uiState.update { it.copy(selectedIceLevel = level) }

    fun toggleTopping(topping: Topping) {
        _uiState.update { state ->
            val current = state.selectedToppings.toMutableList()
            if (current.any { it.id == topping.id }) {
                current.removeAll { it.id == topping.id }
            } else {
                current.add(topping)
            }
            state.copy(selectedToppings = current)
        }
    }

    fun increaseQty() = _uiState.update { it.copy(quantity = it.quantity + 1) }
    fun decreaseQty() = _uiState.update { it.copy(quantity = (it.quantity - 1).coerceAtLeast(1)) }

    fun buildCartItem(): CartItem? {
        val state = _uiState.value
        val product = state.product ?: return null

        val thongTinDaGop = if (state.selectedIcePack == IcePackOption.DA_RIENG) {
            IcePackOption.DA_RIENG.label
        } else {
            "${IcePackOption.DA_CHUNG.label} (${state.selectedIceLevel.label})"
        }

        return CartItem(
            cartItemId        = UUID.randomUUID().toString(),
            productId         = product.id,
            productName       = product.name,
            productImageUrl   = product.imageUrl,
            size              = state.selectedSize,
            sugarLevel        = state.selectedSugar,
            iceLevel          = state.selectedIceLevel,
            selectedToppings  = state.selectedToppings,
            unitPrice         = state.currentUnitPrice,
            quantity          = state.quantity,
            note              = if (state.note.isBlank()) "Đá: $thongTinDaGop" else "Đá: $thongTinDaGop | ${state.note}"
        )
    }

    fun onAddedToCart() = _uiState.update { it.copy(addedToCart = true) }
    fun onAddedToCartHandled() = _uiState.update { it.copy(addedToCart = false) }
    fun onNoteChange(newNote: String) = _uiState.update { it.copy(note = newNote) }
}