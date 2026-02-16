package com.example.ulyssesway.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ulyssesway.data.RulesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import androidx.compose.material.icons.filled.Info

// Data class for Rules
data class TradingRules(
    val dailyLossLimit: Double = 0.0,
    val maxDollarPerTrade: Double = 0.0,
    val maxOpenTickers: Int = 0,
    val dailyLossLimitEnabled: Boolean = false,
    val maxDollarPerTradeEnabled: Boolean = false,
    val limitOrdersOnlyEnabled: Boolean = false,
    val maxOpenTickersEnabled: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesScreen(navController: NavController) {
    val aegeanBlue = Color(0xFF6FA8C8)
    val context = androidx.compose.ui.platform.LocalContext.current
    val rulesDataStore = remember { RulesDataStore(context) }
    val scope = rememberCoroutineScope()

    // State for rules
    var dailyLossLimit by remember { mutableStateOf("500") }
    var maxDollarPerTrade by remember { mutableStateOf("5000") }
    var maxOpenTickers by remember { mutableStateOf("5") }

    var dailyLossEnabled by remember { mutableStateOf(true) }
    var maxDollarEnabled by remember { mutableStateOf(true) }
    var limitOrdersEnabled by remember { mutableStateOf(true) }
    var maxTickersEnabled by remember { mutableStateOf(true) }

    var showSaveSuccess by remember { mutableStateOf(false) }

    // Track original saved values to detect changes
    var savedDailyLossLimit by remember { mutableStateOf("500") }
    var savedMaxDollarPerTrade by remember { mutableStateOf("5000") }
    var savedMaxOpenTickers by remember { mutableStateOf("5") }
    var savedDailyLossEnabled by remember { mutableStateOf(true) }
    var savedMaxDollarEnabled by remember { mutableStateOf(true) }
    var savedLimitOrdersEnabled by remember { mutableStateOf(true) }
    var savedMaxTickersEnabled by remember { mutableStateOf(true) }

    // Check if there are unsaved changes
    val hasUnsavedChanges = dailyLossLimit != savedDailyLossLimit ||
            maxDollarPerTrade != savedMaxDollarPerTrade ||
            maxOpenTickers != savedMaxOpenTickers ||
            dailyLossEnabled != savedDailyLossEnabled ||
            maxDollarEnabled != savedMaxDollarEnabled ||
            limitOrdersEnabled != savedLimitOrdersEnabled ||
            maxTickersEnabled != savedMaxTickersEnabled

    // Load saved rules when screen opens
    LaunchedEffect(Unit) {
        val savedRules = rulesDataStore.getRules()
        dailyLossLimit = savedRules.dailyLossLimit.toString()
        maxDollarPerTrade = savedRules.maxDollarPerTrade.toString()
        maxOpenTickers = savedRules.maxOpenTickers.toString()

        dailyLossEnabled = savedRules.dailyLossLimitEnabled
        maxDollarEnabled = savedRules.maxDollarPerTradeEnabled
        limitOrdersEnabled = savedRules.limitOrdersOnlyEnabled
        maxTickersEnabled = savedRules.maxOpenTickersEnabled

        // Store original values
        savedDailyLossLimit = savedRules.dailyLossLimit.toString()
        savedMaxDollarPerTrade = savedRules.maxDollarPerTrade.toString()
        savedMaxOpenTickers = savedRules.maxOpenTickers.toString()
        savedDailyLossEnabled = savedRules.dailyLossLimitEnabled
        savedMaxDollarEnabled = savedRules.maxDollarPerTradeEnabled
        savedLimitOrdersEnabled = savedRules.limitOrdersOnlyEnabled
        savedMaxTickersEnabled = savedRules.maxOpenTickersEnabled
    }

    // Check if rules are locked
    val isLocked = isRulesLocked()
    val lockInfo = getLockInfo()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    "Trading Rules",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = aegeanBlue,
                titleContentColor = Color.White
            )
        )

        // Lock Status Banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = if (isLocked) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = null,
                        tint = if (isLocked) Color(0xFFC62828) else Color(0xFF2E7D32),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isLocked) "Rules Locked" else "Rules Unlocked",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isLocked) Color(0xFFC62828) else Color(0xFF2E7D32)
                            )
                        )
                        Text(
                            text = lockInfo,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF666666)
                            )
                        )
                    }
                }
            }
        }

        // Unsaved Changes Indicator
        if (hasUnsavedChanges && !isLocked) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFFFF3E0)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFFF57C00),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "You have unsaved changes",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFFF57C00),
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }

        // Scrollable Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Introduction Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Discipline Through Rules",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = aegeanBlue
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Set your trading rules before market open (9:30 AM EST). Rules lock during trading hours to enforce discipline.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.Gray
                        )
                    )
                }
            }

            // Rule #2: Daily Loss Limit
            RuleCard(
                title = "Rule #2: Daily Loss Limit",
                description = "Stop trading when daily loss exceeds this amount. Only liquidations allowed.",
                enabled = dailyLossEnabled,
                onEnabledChange = { dailyLossEnabled = it },
                isLocked = isLocked
            ) {
                OutlinedTextField(
                    value = dailyLossLimit,
                    onValueChange = { dailyLossLimit = it },
                    label = { Text("Maximum Daily Loss ($)") },
                    placeholder = { Text("500") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLocked && dailyLossEnabled,
                    leadingIcon = { Text("$", modifier = Modifier.padding(start = 12.dp)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = aegeanBlue,
                        focusedLabelColor = aegeanBlue
                    )
                )
            }

            // Rule #3: Max Dollar Per Trade
            RuleCard(
                title = "Rule #3: Max $ Per Trade",
                description = "Maximum position size for any single trade (shares × price).",
                enabled = maxDollarEnabled,
                onEnabledChange = { maxDollarEnabled = it },
                isLocked = isLocked
            ) {
                OutlinedTextField(
                    value = maxDollarPerTrade,
                    onValueChange = { maxDollarPerTrade = it },
                    label = { Text("Max Position Value ($)") },
                    placeholder = { Text("5000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLocked && maxDollarEnabled,
                    leadingIcon = { Text("$", modifier = Modifier.padding(start = 12.dp)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = aegeanBlue,
                        focusedLabelColor = aegeanBlue
                    )
                )
            }

            // Rule #4: Limit Orders Only for Adding
            RuleCard(
                title = "Rule #4: Limit Orders for Adding Positions",
                description = "When adding to an existing position, only LIMIT orders allowed. Market orders OK for new positions and liquidations.",
                enabled = limitOrdersEnabled,
                onEnabledChange = { limitOrdersEnabled = it },
                isLocked = isLocked
            ) {
                // No additional input needed - this is a toggle rule
            }

            // Rule #5: Max Open Tickers
            RuleCard(
                title = "Rule #5: Max Open Tickers",
                description = "Limit number of different stocks/tickers you can hold at once.",
                enabled = maxTickersEnabled,
                onEnabledChange = { maxTickersEnabled = it },
                isLocked = isLocked
            ) {
                OutlinedTextField(
                    value = maxOpenTickers,
                    onValueChange = { maxOpenTickers = it },
                    label = { Text("Max Open Positions") },
                    placeholder = { Text("5") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLocked && maxTickersEnabled,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = aegeanBlue,
                        focusedLabelColor = aegeanBlue
                    )
                )
            }

            // Save Button
            Button(
                onClick = {
                    scope.launch {
                        val rules = TradingRules(
                            dailyLossLimit = dailyLossLimit.toDoubleOrNull() ?: 500.0,
                            maxDollarPerTrade = maxDollarPerTrade.toDoubleOrNull() ?: 5000.0,
                            maxOpenTickers = maxOpenTickers.toIntOrNull() ?: 5,
                            dailyLossLimitEnabled = dailyLossEnabled,
                            maxDollarPerTradeEnabled = maxDollarEnabled,
                            limitOrdersOnlyEnabled = limitOrdersEnabled,
                            maxOpenTickersEnabled = maxTickersEnabled
                        )
                        rulesDataStore.saveRules(rules)

                        // Update saved values to match current
                        savedDailyLossLimit = dailyLossLimit
                        savedMaxDollarPerTrade = maxDollarPerTrade
                        savedMaxOpenTickers = maxOpenTickers
                        savedDailyLossEnabled = dailyLossEnabled
                        savedMaxDollarEnabled = maxDollarEnabled
                        savedLimitOrdersEnabled = limitOrdersEnabled
                        savedMaxTickersEnabled = maxTickersEnabled

                        showSaveSuccess = true
                        // Hide success message after 3 seconds
                        kotlinx.coroutines.delay(3000)
                        showSaveSuccess = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLocked,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (hasUnsavedChanges) Color(0xFFF57C00) else aegeanBlue,
                    disabledContainerColor = Color.LightGray
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLocked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Locked - Cannot Save Until Market Close")
                } else {
                    Text(
                        if (hasUnsavedChanges) "Save Changes" else "Save Trading Rules",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // Success Message
            if (showSaveSuccess) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE8F5E9)
                    )
                ) {
                    Text(
                        text = "✓ Rules saved successfully!",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Warning Message
            if (isLocked) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    )
                ) {
                    Text(
                        text = "⚠️ Rules are locked during trading hours. Any changes will be lost. Edit before 9:30 AM EST tomorrow.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFFC62828)
                        ),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun RuleCard(
    title: String,
    description: String,
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    isLocked: Boolean,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) Color.White else Color(0xFFF5F5F5)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.Gray
                        )
                    )
                }

                Switch(
                    checked = enabled,
                    onCheckedChange = onEnabledChange,
                    enabled = !isLocked
                )
            }

            if (enabled) {
                Spacer(modifier = Modifier.height(16.dp))
                content()
            }
        }
    }
}

// Helper functions to check lock status
fun isRulesLocked(): Boolean {
    val now = LocalDateTime.now(ZoneId.of("America/New_York"))
    val dayOfWeek = now.dayOfWeek

    // Not locked on weekends
    if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
        return false
    }

    // Locked after 9:30 AM EST on weekdays
    val marketOpen = now.withHour(9).withMinute(30).withSecond(0)
    return now.isAfter(marketOpen)
}

fun getLockInfo(): String {
    val now = LocalDateTime.now(ZoneId.of("America/New_York"))
    val dayOfWeek = now.dayOfWeek
    val formatter = DateTimeFormatter.ofPattern("h:mm a")
    val currentTime = now.format(formatter)

    return if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
        "Weekend - Edit anytime | Current: $currentTime EST"
    } else {
        val marketOpen = now.withHour(9).withMinute(30).withSecond(0)
        if (now.isBefore(marketOpen)) {
            "Unlocks at 9:30 AM EST | Current: $currentTime EST"
        } else {
            "Locked until Monday 9:30 AM EST | Current: $currentTime EST"
        }
    }
}