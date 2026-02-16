package com.example.ulyssesway.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlin.math.max
import kotlin.math.min

// Mock intraday price data
data class IntradayPrice(
    val time: String,
    val price: Double
)

// Mock stock detail data
data class StockDetail(
    val symbol: String,
    val name: String,
    val currentPrice: Double,
    val change: Double,
    val changePercent: Double,
    val dayHigh: Double,
    val dayLow: Double,
    val previousClose: Double,
    val intradayPrices: List<IntradayPrice>
)

// Mock data - In real app, this would come from Schwab API
fun getMockStockDetail(symbol: String): StockDetail {
    return when (symbol) {
        "AAPL" -> StockDetail(
            symbol = "AAPL",
            name = "Apple Inc.",
            currentPrice = 192.43,
            change = 1.21,
            changePercent = 0.63,
            dayHigh = 193.50,
            dayLow = 190.75,
            previousClose = 191.22,
            intradayPrices = generateMockIntradayData(191.22, 192.43)
        )
        "TSLA" -> StockDetail(
            symbol = "TSLA",
            name = "Tesla Inc.",
            currentPrice = 224.90,
            change = -3.18,
            changePercent = -1.39,
            dayHigh = 228.50,
            dayLow = 223.00,
            previousClose = 228.08,
            intradayPrices = generateMockIntradayData(228.08, 224.90)
        )
        "MSFT" -> StockDetail(
            symbol = "MSFT",
            name = "Microsoft Corp.",
            currentPrice = 411.27,
            change = 2.04,
            changePercent = 0.50,
            dayHigh = 412.80,
            dayLow = 408.50,
            previousClose = 409.23,
            intradayPrices = generateMockIntradayData(409.23, 411.27)
        )
        "NVDA" -> StockDetail(
            symbol = "NVDA",
            name = "NVIDIA Corp.",
            currentPrice = 615.32,
            change = -5.87,
            changePercent = -0.95,
            dayHigh = 622.00,
            dayLow = 614.00,
            previousClose = 621.19,
            intradayPrices = generateMockIntradayData(621.19, 615.32)
        )
        else -> StockDetail(
            symbol = symbol,
            name = "Unknown Company",
            currentPrice = 100.0,
            change = 0.0,
            changePercent = 0.0,
            dayHigh = 102.0,
            dayLow = 98.0,
            previousClose = 100.0,
            intradayPrices = generateMockIntradayData(100.0, 100.0)
        )
    }
}

// Generate realistic intraday price movement
fun generateMockIntradayData(open: Double, close: Double): List<IntradayPrice> {
    val prices = mutableListOf<IntradayPrice>()
    val times = listOf("9:30", "10:00", "10:30", "11:00", "11:30", "12:00",
        "12:30", "1:00", "1:30", "2:00", "2:30", "3:00", "3:30", "4:00")

    var currentPrice = open
    val totalChange = close - open
    val stepChange = totalChange / (times.size - 1)

    times.forEachIndexed { index, time ->
        // Add some randomness but trend toward close
        val randomVariation = (Math.random() - 0.5) * (open * 0.01)
        currentPrice = open + (stepChange * index) + randomVariation
        prices.add(IntradayPrice(time, currentPrice))
    }

    // Make sure last price is close
    prices[prices.size - 1] = IntradayPrice("4:00", close)

    return prices
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockDetailScreen(navController: NavController, symbol: String) {
    val aegeanBlue = Color(0xFF6FA8C8)
    val gainGreen = Color(0xFF4CAF50)
    val lossRed = Color(0xFFEF5350)

    // Load stock data
    val stockDetail = remember { getMockStockDetail(symbol) }
    val changeColor = if (stockDetail.change >= 0) gainGreen else lossRed

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    stockDetail.symbol,
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stock Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = stockDetail.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.Gray
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "\$${String.format("%.2f", stockDetail.currentPrice)}",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 40.sp,
                                color = Color(0xFF333333)
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            imageVector = if (stockDetail.change >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = changeColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = changeColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "${if (stockDetail.change >= 0) "+" else ""}${String.format("%.2f", stockDetail.change)} (${if (stockDetail.changePercent >= 0) "+" else ""}${String.format("%.2f", stockDetail.changePercent)}%)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = changeColor,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Today's Chart
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Today's Chart",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    IntradayChart(
                        prices = stockDetail.intradayPrices,
                        color = changeColor
                    )
                }
            }

            // Today's Range
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Today's Range",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    PriceRangeSlider(
                        low = stockDetail.dayLow,
                        high = stockDetail.dayHigh,
                        current = stockDetail.currentPrice,
                        color = aegeanBlue
                    )
                }
            }

            // Trade Button
            Button(
                onClick = {
                    // Navigate to Trade screen with symbol pre-filled
                    navController.navigate("trade?symbol=${symbol}")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = aegeanBlue
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Trade $symbol",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun IntradayChart(prices: List<IntradayPrice>, color: Color) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        if (prices.isEmpty()) return@Canvas

        val priceValues = prices.map { it.price }
        val minPrice = priceValues.minOrNull() ?: 0.0
        val maxPrice = priceValues.maxOrNull() ?: 0.0
        val priceRange = maxPrice - minPrice

        if (priceRange == 0.0) return@Canvas

        val width = size.width
        val height = size.height
        val spacing = width / (prices.size - 1)

        // Create path for area chart
        val path = Path()

        // Start from bottom left
        path.moveTo(0f, height)

        // Draw to first point
        val firstY = height - ((prices[0].price - minPrice) / priceRange * height).toFloat()
        path.lineTo(0f, firstY)

        // Draw line through all points
        prices.forEachIndexed { index, price ->
            val x = index * spacing
            val y = height - ((price.price - minPrice) / priceRange * height).toFloat()
            path.lineTo(x, y)
        }

        // Complete the area back to bottom right
        path.lineTo(width, height)
        path.close()

        // Draw filled area
        drawPath(
            path = path,
            color = color.copy(alpha = 0.2f),
            style = Fill
        )

        // Draw line on top
        val linePath = Path()
        prices.forEachIndexed { index, price ->
            val x = index * spacing
            val y = height - ((price.price - minPrice) / priceRange * height).toFloat()

            if (index == 0) {
                linePath.moveTo(x, y)
            } else {
                linePath.lineTo(x, y)
            }
        }

        drawPath(
            path = linePath,
            color = color,
            style = Stroke(width = 3f)
        )
    }
}

@Composable
fun PriceRangeSlider(low: Double, high: Double, current: Double, color: Color) {
    Column {
        // Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Low: \$${String.format("%.2f", low)}",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.Gray
                )
            )
            Text(
                text = "High: \$${String.format("%.2f", high)}",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.Gray
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Range bar with indicator
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
        ) {
            // Background bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .align(Alignment.Center),
                shape = RoundedCornerShape(4.dp),
                color = Color.LightGray
            ) {}

            // Current price indicator
            val range = high - low
            val position = if (range > 0) ((current - low) / range).toFloat() else 0.5f
            val clampedPosition = position.coerceIn(0f, 1f)

            // Indicator circle - using this@BoxWithConstraints explicitly
            Surface(
                modifier = Modifier
                    .offset(x = this@BoxWithConstraints.maxWidth * clampedPosition - 12.dp)
                    .size(24.dp)
                    .align(Alignment.CenterStart),
                shape = RoundedCornerShape(12.dp),
                color = color,
                shadowElevation = 4.dp
            ) {}
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Current price
        Text(
            text = "Current: \$${String.format("%.2f", current)}",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            ),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}