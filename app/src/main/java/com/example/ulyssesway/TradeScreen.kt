package com.example.ulyssesway.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ulyssesway.data.*
import kotlinx.coroutines.launch
import com.example.ulyssesway.data.OrderType
import com.example.ulyssesway.data.TradeRequest
import com.example.ulyssesway.data.RulesEnforcer
import com.example.ulyssesway.data.PositionsDataStore
import com.example.ulyssesway.data.DailyPnL

enum class TradeAction {
    BUY, SELL, SELL_SHORT, BUY_TO_COVER
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradeScreen(prefilledSymbol: String = "AAPL") {
    val aegeanBlue = Color(0xFF6FA8C8)
    val gainGreen = Color(0xFF4CAF50)
    val lossRed = Color(0xFFEF5350)

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val rulesEnforcer: RulesEnforcer = remember { RulesEnforcer(context) }
    val positionsDataStore: PositionsDataStore = remember { PositionsDataStore(context) }

    // Trade form state
    var symbol by remember { mutableStateOf(prefilledSymbol) }
    var quantity by remember { mutableStateOf("") }
    var limitPrice by remember { mutableStateOf("") }
    var selectedAction by remember { mutableStateOf(TradeAction.BUY) }
    var selectedOrderType by remember { mutableStateOf(OrderType.MARKET) }

    // Validation and status
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var currentPrice by remember { mutableStateOf(0.0) }
    var suggestedAction by remember { mutableStateOf("") }
    var isLiquidationMode by remember { mutableStateOf(false) }
    var dailyPnL by remember { mutableStateOf<DailyPnL?>(null) }
    var existingPosition by remember { mutableStateOf<com.example.ulyssesway.data.Position?>(null) }

    // Load daily P&L and check liquidation mode
    LaunchedEffect(Unit) {
        isLiquidationMode = rulesEnforcer.isLiquidationOnlyMode()
        dailyPnL = rulesEnforcer.getCurrentDailyPnL()
    }

    // Update suggested action when symbol changes
    LaunchedEffect(symbol) {
        if (symbol.isNotEmpty()) {
            val upperSymbol = symbol.uppercase()
            suggestedAction = rulesEnforcer.getSuggestedAction(upperSymbol)
            existingPosition = positionsDataStore.getPosition(upperSymbol)

            // Mock current price - in real app, this would stream from Schwab API
            currentPrice = when (symbol.uppercase()) {
                "AAPL" -> 192.43
                "TSLA" -> 224.90
                "MSFT" -> 411.27
                "NVDA" -> 615.32
                else -> 0.0
            }
        }
    }

    // Calculate estimated total
    val estimatedTotal = remember(quantity, currentPrice, limitPrice, selectedOrderType) {
        val shares = quantity.toIntOrNull() ?: 0
        val price = if (selectedOrderType == OrderType.LIMIT) {
            limitPrice.toDoubleOrNull() ?: 0.0
        } else {
            currentPrice
        }
        shares * price
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Place Trade",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )
        )

        // Liquidation Mode Warning
        if (isLiquidationMode) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⚠️ LIQUIDATION-ONLY MODE",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = lossRed,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Daily loss limit exceeded. Only closing trades allowed.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = lossRed
                        )
                    )
                    dailyPnL?.let { pnl ->
                        Text(
                            text = "Daily P&L: -\$${String.format("%.2f", -pnl.totalPnL)}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = lossRed,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
        }

        // Ticker Display Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Get ticker name
                val tickerName = when (symbol.uppercase()) {
                    "AAPL" -> "Apple Inc."
                    "TSLA" -> "Tesla Inc."
                    "MSFT" -> "Microsoft Corp."
                    "NVDA" -> "NVIDIA Corp."
                    else -> "Unknown Company"
                }

                // Ticker Symbol
                Text(
                    text = symbol.uppercase(),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Company Name
                Text(
                    text = tickerName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.Gray
                    )
                )

                if (currentPrice > 0) {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    // Current Price (streaming simulation)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Current Price",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Gray
                            )
                        )
                        Text(
                            text = "\$${String.format("%.2f", currentPrice)}",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = aegeanBlue
                            )
                        )
                    }
                }

                if (suggestedAction.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFFF3E0)
                    ) {
                        Text(
                            text = suggestedAction,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFF57C00)
                            ),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }

        // Trade Action Selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Action",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // BUY
                    FilterChip(
                        selected = selectedAction == TradeAction.BUY,
                        onClick = { selectedAction = TradeAction.BUY },
                        label = { Text("BUY") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = aegeanBlue,
                            selectedLabelColor = Color.White
                        )
                    )

                    // SELL
                    FilterChip(
                        selected = selectedAction == TradeAction.SELL,
                        onClick = { selectedAction = TradeAction.SELL },
                        label = { Text("SELL") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = aegeanBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // SELL SHORT
                    FilterChip(
                        selected = selectedAction == TradeAction.SELL_SHORT,
                        onClick = { selectedAction = TradeAction.SELL_SHORT },
                        label = { Text("SELL SHORT") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = aegeanBlue,
                            selectedLabelColor = Color.White
                        )
                    )

                    // BUY TO COVER
                    FilterChip(
                        selected = selectedAction == TradeAction.BUY_TO_COVER,
                        onClick = { selectedAction = TradeAction.BUY_TO_COVER },
                        label = { Text("BUY TO COVER") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = aegeanBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Order Type Selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Order Type",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedOrderType == OrderType.MARKET,
                        onClick = {
                            // Check Rule #4: Limit orders required for adding to positions
                            if (existingPosition != null) {
                                errorMessage = "Adding to existing position requires LIMIT order"
                            } else {
                                selectedOrderType = OrderType.MARKET
                                errorMessage = null
                            }
                        },
                        label = { Text("MARKET") },
                        modifier = Modifier.weight(1f),
                        enabled = existingPosition == null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = aegeanBlue,
                            selectedLabelColor = Color.White
                        )
                    )

                    FilterChip(
                        selected = selectedOrderType == OrderType.LIMIT,
                        onClick = {
                            selectedOrderType = OrderType.LIMIT
                            errorMessage = null
                        },
                        label = { Text("LIMIT") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = aegeanBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                if (existingPosition != null && selectedOrderType == OrderType.MARKET) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "⚠️ You have an existing position. Use LIMIT orders to add.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFF57C00)
                        )
                    )
                }
            }
        }

        // Quantity and Price Inputs
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Quantity
                Text(
                    text = "Quantity",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = quantity,
                    onValueChange = {
                        quantity = it
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Number of shares") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = aegeanBlue,
                        focusedLabelColor = aegeanBlue
                    )
                )

                // Limit Price (only show when LIMIT is selected)
                if (selectedOrderType == OrderType.LIMIT) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Limit Price",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = limitPrice,
                        onValueChange = {
                            limitPrice = it
                            errorMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Price per share") },
                        leadingIcon = { Text("\$") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = aegeanBlue,
                            focusedLabelColor = aegeanBlue
                        )
                    )
                }

                // Estimated Total
                if (estimatedTotal > 0) {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Estimated Total:",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "\$${String.format("%,.2f", estimatedTotal)}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = aegeanBlue
                            )
                        )
                    }
                }
            }
        }

        // Error Message
        if (errorMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
            ) {
                Text(
                    text = errorMessage!!,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = lossRed
                    ),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // Success Message
        if (successMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
            ) {
                Text(
                    text = successMessage!!,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = gainGreen,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // Submit Button
        Button(
            onClick = {
                scope.launch {
                    errorMessage = null
                    successMessage = null

                    // Validate inputs
                    if (symbol.isEmpty()) {
                        errorMessage = "Please select a stock"
                        return@launch
                    }
                    if (quantity.toIntOrNull() == null || quantity.toInt() <= 0) {
                        errorMessage = "Please enter a valid quantity"
                        return@launch
                    }
                    if (selectedOrderType == OrderType.LIMIT &&
                        (limitPrice.toDoubleOrNull() == null || limitPrice.toDouble() <= 0)) {
                        errorMessage = "Please enter a valid limit price"
                        return@launch
                    }

                    // Determine trade direction
                    val tradeQuantity = when (selectedAction) {
                        TradeAction.BUY, TradeAction.BUY_TO_COVER -> quantity.toInt()
                        TradeAction.SELL, TradeAction.SELL_SHORT -> -quantity.toInt()
                    }

                    val price = if (selectedOrderType == OrderType.LIMIT) {
                        limitPrice.toDouble()
                    } else {
                        currentPrice
                    }

                    // Determine if closing trade
                    val isClosing = when (selectedAction) {
                        TradeAction.SELL -> existingPosition?.isLong == true
                        TradeAction.BUY_TO_COVER -> existingPosition?.isShort == true
                        else -> false
                    }

                    // Create trade request
                    val tradeRequest = TradeRequest(
                        symbol = symbol.uppercase(),
                        quantity = tradeQuantity,
                        price = price,
                        orderType = selectedOrderType,
                        isClosingTrade = isClosing
                    )

                    // Validate with rules enforcer
                    val validationResult = rulesEnforcer.validateTrade(tradeRequest)

                    if (validationResult.isValid) {
                        // Trade is valid - would execute here
                        successMessage = "✓ Trade validated! (Mock execution - rules passed)"

                        // In real app: execute trade via Schwab API
                        // Then update positions
                    } else {
                        // Show violation messages
                        errorMessage = validationResult.errorMessage
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = symbol.isNotEmpty() && quantity.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(
                containerColor = aegeanBlue
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                "Review Order",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}