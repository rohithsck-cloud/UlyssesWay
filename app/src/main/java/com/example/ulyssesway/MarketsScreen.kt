package com.example.ulyssesway.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Data class for market index
data class MarketIndex(
    val name: String,
    val symbol: String,
    val value: Double,
    val change: Double,
    val percentChange: Double
)

// Mock market data
val marketIndices = listOf(
    MarketIndex(
        name = "S&P 500",
        symbol = "SPX",
        value = 5026.61,
        change = 28.70,
        percentChange = 0.57
    ),
    MarketIndex(
        name = "Dow Jones",
        symbol = "DJI",
        value = 38996.39,
        change = -23.39,
        percentChange = -0.06
    ),
    MarketIndex(
        name = "NASDAQ",
        symbol = "IXIC",
        value = 15927.90,
        change = 126.67,
        percentChange = 0.80
    ),
    MarketIndex(
        name = "Russell 2000",
        symbol = "RUT",
        value = 2124.55,
        change = 15.22,
        percentChange = 0.72
    ),
    MarketIndex(
        name = "VIX",
        symbol = "VIX",
        value = 13.45,
        change = -0.87,
        percentChange = -6.08
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketsScreen() {
    // Colors
    val aegeanBlue = Color(0xFF6FA8C8)
    val gainGreen = Color(0xFF4CAF50)
    val lossRed = Color(0xFFEF5350)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {
        // Top Bar
        Surface(
            color = aegeanBlue,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Markets",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Major Market Indices",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.9f)
                    )
                )
            }
        }

        // Market Summary Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = aegeanBlue
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Market Status",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = gainGreen
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Markets Open",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Trading hours: 9:30 AM - 4:00 PM EST",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.7f)
                    )
                )
            }
        }

        // Indices List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Indices",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    ),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(marketIndices) { index ->
                MarketIndexCard(
                    index = index,
                    gainGreen = gainGreen,
                    lossRed = lossRed
                )
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun MarketIndexCard(
    index: MarketIndex,
    gainGreen: Color,
    lossRed: Color
) {
    val changeColor = if (index.change >= 0) gainGreen else lossRed
    val changeIcon = if (index.change >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown
    val isGain = index.change >= 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Index name and symbol with icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Trend Icon
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = changeColor.copy(alpha = 0.1f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = changeIcon,
                            contentDescription = null,
                            tint = changeColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = index.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = index.symbol,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.Gray
                        )
                    )
                }
            }

            // Right: Value and change
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = String.format("%,.2f", index.value),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333),
                        fontSize = 20.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Change badge
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = changeColor.copy(alpha = 0.1f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = formatChange(index.change),
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = changeColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = formatPercentChange(index.percentChange),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = changeColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
        }
    }
}

// Helper functions
private fun formatChange(change: Double): String {
    val sign = if (change >= 0) "+" else ""
    return "$sign${String.format("%.2f", change)}"
}

private fun formatPercentChange(percent: Double): String {
    val sign = if (percent >= 0) "+" else ""
    return "$sign${String.format("%.2f", percent)}%"
}