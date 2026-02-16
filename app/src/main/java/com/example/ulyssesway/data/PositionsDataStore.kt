package com.example.ulyssesway.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

private val Context.positionsDataStore: DataStore<Preferences> by preferencesDataStore(name = "positions")

// Data classes for positions
data class Position(
    val symbol: String,
    val quantity: Int,  // Positive for long, negative for short
    val averageCost: Double,
    val currentPrice: Double
) {
    val marketValue: Double
        get() = quantity * currentPrice

    val costBasis: Double
        get() = quantity * averageCost

    val unrealizedPnL: Double
        get() = marketValue - costBasis

    val isLong: Boolean
        get() = quantity > 0

    val isShort: Boolean
        get() = quantity < 0
}

data class Trade(
    val symbol: String,
    val quantity: Int,  // Positive for buy, negative for sell
    val price: Double,
    val timestamp: Long = System.currentTimeMillis()
) {
    val value: Double
        get() = kotlin.math.abs(quantity) * price
}

data class DailyPnL(
    val realizedPnL: Double,
    val unrealizedPnL: Double,
    val totalPnL: Double,
    val startingValue: Double,
    val currentValue: Double
)

class PositionsDataStore(private val context: Context) {

    private val gson = Gson()

    companion object {
        private val POSITIONS_KEY = stringPreferencesKey("positions")
        private val TODAYS_TRADES_KEY = stringPreferencesKey("todays_trades")
        private val MARKET_OPEN_VALUE_KEY = doublePreferencesKey("market_open_value")
        private val LAST_RESET_DATE_KEY = stringPreferencesKey("last_reset_date")
    }

    // Get all open positions
    suspend fun getPositions(): List<Position> {
        val preferences: Preferences = context.positionsDataStore.data.first()
        val json = preferences[POSITIONS_KEY] ?: return emptyList()

        val type = object : TypeToken<List<Position>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    // Get position for specific symbol
    suspend fun getPosition(symbol: String): Position? {
        return getPositions().find { it.symbol == symbol }
    }

    // Check if position exists for symbol
    suspend fun hasPosition(symbol: String): Boolean {
        return getPosition(symbol) != null
    }

    // Count number of open tickers (unique symbols with positions)
    suspend fun getOpenTickersCount(): Int {
        return getPositions().size
    }

    // Get all today's trades
    suspend fun getTodaysTrades(): List<Trade> {
        val preferences: Preferences = context.positionsDataStore.data.first()
        val json = preferences[TODAYS_TRADES_KEY] ?: return emptyList()

        val type = object : TypeToken<List<Trade>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    // Calculate daily P&L
    suspend fun getDailyPnL(): DailyPnL {
        val positions = getPositions()
        val trades = getTodaysTrades()
        val preferences: Preferences = context.positionsDataStore.data.first()
        val marketOpenValue = preferences[MARKET_OPEN_VALUE_KEY] ?: 0.0

        // Calculate realized P&L from today's trades
        val realizedPnL = calculateRealizedPnL(trades)

        // Calculate unrealized P&L from current positions
        val unrealizedPnL = positions.sumOf { it.unrealizedPnL }

        // Calculate current total value
        val currentValue = positions.sumOf { it.marketValue }

        // Total P&L = change from market open
        val totalPnL = (currentValue - marketOpenValue) + realizedPnL

        return DailyPnL(
            realizedPnL = realizedPnL,
            unrealizedPnL = unrealizedPnL,
            totalPnL = totalPnL,
            startingValue = marketOpenValue,
            currentValue = currentValue
        )
    }

    // Calculate realized P&L from trades
    private fun calculateRealizedPnL(trades: List<Trade>): Double {
        // Group trades by symbol and calculate P&L
        val tradesBySymbol = trades.groupBy { it.symbol }
        var totalRealizedPnL = 0.0

        tradesBySymbol.forEach { (symbol, symbolTrades) ->
            // Simple FIFO calculation
            var costBasis = 0.0
            var shares = 0

            symbolTrades.forEach { trade ->
                if (trade.quantity > 0) {
                    // Buy - add to cost basis
                    costBasis += trade.value
                    shares += trade.quantity
                } else {
                    // Sell - realize gain/loss
                    val sellValue = trade.value
                    val avgCost = if (shares > 0) costBasis / shares else 0.0
                    val soldShares = kotlin.math.abs(trade.quantity)
                    val soldCost = avgCost * soldShares

                    totalRealizedPnL += sellValue - soldCost

                    // Update remaining
                    costBasis -= soldCost
                    shares -= soldShares
                }
            }
        }

        return totalRealizedPnL
    }

    // Add or update a position
    suspend fun updatePosition(position: Position) {
        context.positionsDataStore.edit { preferences ->
            val positions = getPositions().toMutableList()

            // Remove existing position for this symbol
            positions.removeAll { it.symbol == position.symbol }

            // Add new position if quantity is not zero
            if (position.quantity != 0) {
                positions.add(position)
            }

            preferences[POSITIONS_KEY] = gson.toJson(positions)
        }
    }

    // Record a trade
    suspend fun addTrade(trade: Trade) {
        context.positionsDataStore.edit { preferences ->
            val trades = getTodaysTrades().toMutableList()
            trades.add(trade)
            preferences[TODAYS_TRADES_KEY] = gson.toJson(trades)
        }
    }

    // Set market open value (called at market open)
    suspend fun setMarketOpenValue(value: Double) {
        context.positionsDataStore.edit { preferences ->
            preferences[MARKET_OPEN_VALUE_KEY] = value
            preferences[LAST_RESET_DATE_KEY] = getCurrentDate()
        }
    }

    // Reset daily data (called at market close or new day)
    suspend fun resetDailyData() {
        context.positionsDataStore.edit { preferences ->
            preferences[TODAYS_TRADES_KEY] = gson.toJson(emptyList<Trade>())

            // Set market open value to current portfolio value
            val positions = getPositions()
            val currentValue = positions.sumOf { it.marketValue }
            preferences[MARKET_OPEN_VALUE_KEY] = currentValue
            preferences[LAST_RESET_DATE_KEY] = getCurrentDate()
        }
    }

    // Check if we need to reset (new trading day)
    suspend fun checkAndResetIfNeeded() {
        val preferences: Preferences = context.positionsDataStore.data.first()
        val lastResetDate = preferences[LAST_RESET_DATE_KEY]
        val currentDate = getCurrentDate()

        if (lastResetDate != currentDate) {
            resetDailyData()
        }
    }

    // Get current date string (YYYY-MM-DD)
    private fun getCurrentDate(): String {
        return java.time.LocalDate.now().toString()
    }

    // Clear all positions and trades (for testing/reset)
    suspend fun clearAll() {
        context.positionsDataStore.edit { preferences ->
            preferences.clear()
        }
    }

    // Add mock positions for testing
    suspend fun addMockPositions() {
        val mockPositions = listOf(
            Position("AAPL", 50, 180.00, 192.43),
            Position("TSLA", -25, 230.00, 224.90),  // Short position
            Position("MSFT", 30, 400.00, 411.27)
        )

        context.positionsDataStore.edit { preferences ->
            preferences[POSITIONS_KEY] = gson.toJson(mockPositions)

            // Set market open value
            val startValue = mockPositions.sumOf { it.quantity * it.averageCost }
            preferences[MARKET_OPEN_VALUE_KEY] = startValue
            preferences[LAST_RESET_DATE_KEY] = getCurrentDate()
        }
    }
}