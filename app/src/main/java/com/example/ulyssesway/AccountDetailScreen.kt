package com.example.ulyssesway.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

// Data classes
data class AccountPosition(
    val symbol: String,
    val name: String,
    val quantity: Int,
    val averageCost: Double,
    val currentPrice: Double,
    val marketValue: Double,
    val gainLoss: Double,
    val gainLossPercent: Double
)

data class Transaction(
    val date: String,
    val type: String,
    val symbol: String,
    val quantity: Int,
    val price: Double,
    val amount: Double
)

// Mock data
val mockPositions = listOf(
    AccountPosition("AAPL", "Apple Inc.", 50, 150.00, 192.43, 9621.50, 2121.50, 28.29),
    AccountPosition("TSLA", "Tesla Inc.", 25, 200.00, 224.90, 5622.50, 622.50, 12.45),
    AccountPosition("MSFT", "Microsoft Corp.", 30, 380.00, 411.27, 12338.10, 938.10, 8.23),
    AccountPosition("NVDA", "NVIDIA Corp.", 15, 500.00, 615.32, 9229.80, 1729.80, 23.06)
)

val mockTransactions = listOf(
    Transaction("2024-02-14", "BUY", "AAPL", 10, 192.43, -1924.30),
    Transaction("2024-02-13", "SELL", "TSLA", 5, 224.90, 1124.50),
    Transaction("2024-02-12", "BUY", "MSFT", 15, 411.27, -6169.05),
    Transaction("2024-02-10", "DIVIDEND", "AAPL", 50, 0.24, 12.00)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailScreen(navController: NavController) {
    // Colors
    val aegeanBlue = Color(0xFF6FA8C8)
    val gainGreen = Color(0xFF4CAF50)
    val lossRed = Color(0xFFEF5350)

    // Account summary data
    val accountValue = 36811.90
    val dayChange = 428.75
    val dayChangePercent = 1.18
    val unrealizedGainLoss = 5411.90
    val unrealizedGainLossPercent = 17.24

    // Tab state
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Positions", "Gain/Loss", "History")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    "Brokerage Account",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = aegeanBlue,
                titleContentColor = Color.White
            )
        )

        // Account Summary Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Account Value
                Text(
                    text = "Account Value",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Gray
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$${String.format("%,.2f", accountValue)}",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333),
                        fontSize = 36.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                // Day Change and Unrealized Gain/Loss
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Day Change
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Day Change",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.Gray
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (dayChange >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = if (dayChange >= 0) gainGreen else lossRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text(
                                    text = "${if (dayChange >= 0) "+" else ""}${String.format("%.2f", dayChange)}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (dayChange >= 0) gainGreen else lossRed
                                    )
                                )
                                Text(
                                    text = "${if (dayChangePercent >= 0) "+" else ""}${String.format("%.2f", dayChangePercent)}%",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (dayChange >= 0) gainGreen else lossRed
                                    )
                                )
                            }
                        }
                    }

                    // Divider
                    VerticalDivider(
                        modifier = Modifier
                            .height(60.dp)
                            .padding(horizontal = 8.dp)
                    )

                    // Unrealized Gain/Loss
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Unrealized Gain/Loss",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.Gray
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (unrealizedGainLoss >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = if (unrealizedGainLoss >= 0) gainGreen else lossRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text(
                                    text = "${if (unrealizedGainLoss >= 0) "+" else ""}${String.format("%.2f", unrealizedGainLoss)}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (unrealizedGainLoss >= 0) gainGreen else lossRed
                                    )
                                )
                                Text(
                                    text = "${if (unrealizedGainLossPercent >= 0) "+" else ""}${String.format("%.2f", unrealizedGainLossPercent)}%",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (unrealizedGainLoss >= 0) gainGreen else lossRed
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = aegeanBlue,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = aegeanBlue
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        // Tab Content
        when (selectedTab) {
            0 -> PositionsTab(mockPositions, gainGreen, lossRed)
            1 -> GainLossTab(mockPositions, gainGreen, lossRed)
            2 -> HistoryTab(mockTransactions, gainGreen, lossRed)
        }
    }
}

@Composable
fun PositionsTab(positions: List<AccountPosition>, gainGreen: Color, lossRed: Color) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(positions) { position ->
            PositionCard(position, gainGreen, lossRed)
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun PositionCard(position: AccountPosition, gainGreen: Color, lossRed: Color) {
    val changeColor = if (position.gainLoss >= 0) gainGreen else lossRed

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Symbol and name
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = position.symbol,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = position.name,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.Gray
                        )
                    )
                }

                Text(
                    text = "$${String.format("%,.2f", position.marketValue)}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            // Position details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Quantity: ${position.quantity}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Avg Cost: $${String.format("%.2f", position.averageCost)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Current: $${String.format("%.2f", position.currentPrice)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "${if (position.gainLoss >= 0) "+" else ""}$${String.format("%.2f", position.gainLoss)} (${if (position.gainLossPercent >= 0) "+" else ""}${String.format("%.2f", position.gainLossPercent)}%)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = changeColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun GainLossTab(positions: List<AccountPosition>, gainGreen: Color, lossRed: Color) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(positions.sortedByDescending { it.gainLoss }) { position ->
            GainLossCard(position, gainGreen, lossRed)
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun GainLossCard(position: AccountPosition, gainGreen: Color, lossRed: Color) {
    val changeColor = if (position.gainLoss >= 0) gainGreen else lossRed

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = position.symbol,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "${position.quantity} shares",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.Gray
                    )
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (position.gainLoss >= 0) "+" else ""}$${String.format("%.2f", position.gainLoss)}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = changeColor
                    )
                )
                Text(
                    text = "${if (position.gainLossPercent >= 0) "+" else ""}${String.format("%.2f", position.gainLossPercent)}%",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = changeColor
                    )
                )
            }
        }
    }
}

@Composable
fun HistoryTab(transactions: List<Transaction>, gainGreen: Color, lossRed: Color) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(transactions) { transaction ->
            TransactionCard(transaction, gainGreen, lossRed)
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun TransactionCard(transaction: Transaction, gainGreen: Color, lossRed: Color) {
    val typeColor = when (transaction.type) {
        "BUY" -> lossRed
        "SELL" -> gainGreen
        else -> Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = typeColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = transaction.type,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = typeColor,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = transaction.symbol,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = transaction.date,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.Gray
                    )
                )
                if (transaction.type != "DIVIDEND") {
                    Text(
                        text = "${transaction.quantity} shares @ $${String.format("%.2f", transaction.price)}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.Gray
                        )
                    )
                }
            }

            Text(
                text = "${if (transaction.amount >= 0) "+" else ""}$${String.format("%.2f", transaction.amount)}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (transaction.amount >= 0) gainGreen else Color(0xFF333333)
                )
            )
        }
    }
}