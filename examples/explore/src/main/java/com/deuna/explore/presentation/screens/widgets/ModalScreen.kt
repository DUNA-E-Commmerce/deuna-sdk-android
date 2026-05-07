package com.deuna.explore.presentation.screens.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deuna.explore.data.ProductCatalog
import com.deuna.explore.domain.ExploreProduct
import com.deuna.explore.presentation.ExploreViewModel
import com.deuna.explore.domain.ApmOption

@Composable
fun ModalScreen(viewModel: ExploreViewModel) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val products = state.products
    val selectedIds = state.selectedProductIds
    val useManual = state.useManualOrderTokenFlow
    var showApmDialog by remember { mutableStateOf(false) }

    if (showApmDialog) {
        ApmPickerDialog(
            options = state.apmOptions,
            isLoading = state.isLoadingApms,
            onSelect = { apm ->
                showApmDialog = false
                viewModel.showFormularios(context, apm)
            },
            onDismiss = { showApmDialog = false },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (useManual) {
            ManualCheckoutPreview(products = products)
        } else {
            ProductCatalogSection(
                products = products,
                selectedIds = selectedIds,
                onToggle = { viewModel.toggleProductSelection(it) },
            )
        }

        if (!state.modalStatusMessage.isNullOrEmpty()) {
            Text(
                text = state.modalStatusMessage!!,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Red.copy(alpha = 0.9f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = { viewModel.showModalWidget(context) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLaunchingModalWidget,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF147AE8)),
            ) {
                Text(
                    text = if (state.isLaunchingModalWidget) "Preparing..." else "Show Widget",
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Button(
                onClick = { viewModel.showWallets() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLaunchingWallets,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
            ) {
                Text(
                    text = if (state.isLaunchingWallets) "Preparing..." else "Wallets",
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Button(
                onClick = {
                    viewModel.loadApmOptions()
                    showApmDialog = true
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLaunchingFormularios,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
            ) {
                Text(
                    text = if (state.isLaunchingFormularios) "Preparando..." else "Formularios",
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun ManualCheckoutPreview(products: List<ExploreProduct>) {
    val total = products.sumOf { it.priceInCents }
    val fractionDigits = products.firstOrNull()?.fractionDigits ?: 2
    val symbol = products.firstOrNull()?.currencySymbol ?: "$"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Checkout", fontWeight = FontWeight.SemiBold)
            Row {
                Text("Items", modifier = Modifier.weight(1f))
                Text("${products.size}")
            }
            Row {
                Text("Total", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Text(
                    ProductCatalog.formatPrice(total, fractionDigits, symbol),
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun ProductCatalogSection(
    products: List<ExploreProduct>,
    selectedIds: Set<String>,
    onToggle: (String) -> Unit,
) {
    val selectedTotal = products.filter { it.id in selectedIds }.sumOf { it.priceInCents }
    val fractionDigits = products.firstOrNull()?.fractionDigits ?: 2
    val symbol = products.firstOrNull()?.currencySymbol ?: "$"

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Products", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val chunked = products.chunked(2)
            chunked.forEach { rowProducts ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowProducts.forEach { product ->
                        ProductCard(
                            product = product,
                            isSelected = product.id in selectedIds,
                            onToggle = { onToggle(product.id) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (rowProducts.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Cart (${selectedIds.size})",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
            Text(
                ProductCatalog.formatPrice(selectedTotal, fractionDigits, symbol),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
        }
    }
}

@Composable
private fun ProductCard(
    product: ExploreProduct,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF147AE8).copy(alpha = 0.6f))
        } else null,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = null,
                    tint = Color(0xFF147AE8),
                )
            }
            Text(product.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 2)
            Text(
                ProductCatalog.formatPrice(product.priceInCents, product.fractionDigits, product.currencySymbol),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = onToggle,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) Color(0xFF147AE8).copy(alpha = 0.12f) else Color(0xFF147AE8),
                    contentColor = if (isSelected) Color(0xFF147AE8) else Color.White,
                ),
                contentPadding = PaddingValues(vertical = 9.dp),
            ) {
                Text(if (isSelected) "Added" else "Add", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
        }
    }
}
