package com.example.ulyssesway.data

import android.content.Context
import com.example.ulyssesway.ui.TradingRules

// Trade validation result
sealed class RuleViolation {
    data class DailyLossLimitExceeded(
        val limit: Double,
        val currentLoss: Double
    ) : RuleViolation()

    data class MaxDollarPerTradeExceeded(
        val limit: Double,
        val tradeValue: Double,
        val maxShares: Int
    ) : RuleViolation()

    data class LimitOrderRequired(
        val symbol: String
    ) : RuleViolation()

    data class MaxTickersExceeded(
        val limit: Int,
        val currentCount: Int
    ) : RuleViolation()
}

data class TradeValidationResult(
    val isValid: Boolean,
    val violations: List<RuleViolation> = emptyList(),
    val errorMessage: String? = null
)

// Trade request to validate
data class TradeRequest(
    val symbol: String,
    val quantity: Int,  // Positive for buy, negative for sell
    val price: Double,
    val orderType: OrderType,
    val isClosingTrade: Boolean = false  // True if closing an existing position
)

enum class OrderType {
    MARKET, LIMIT, STOP
}

class RulesEnforcer(private val context: Context) {

    private val rulesDataStore = RulesDataStore(context)
    private val positionsDataStore = PositionsDataStore(context)

    // Main validation function - checks all active rules
    suspend fun validateTrade(trade: TradeRequest): TradeValidationResult {
        val rules = rulesDataStore.getRules()
        val violations = mutableListOf<RuleViolation>()

        // Check if trade is opening a new position or closing
        val isOpening = !trade.isClosingTrade

        // Rule #2: Daily Loss Limit
        if (rules.dailyLossLimitEnabled && isOpening) {
            val dailyLossViolation = checkDailyLossLimit(rules, trade)
            if (dailyLossViolation != null) {
                violations.add(dailyLossViolation)
            }
        }

        // Rule #3: Max Dollar Per Trade
        if (rules.maxDollarPerTradeEnabled && isOpening) {
            val maxDollarViolation = checkMaxDollarPerTrade(rules, trade)
            if (maxDollarViolation != null) {
                violations.add(maxDollarViolation)
            }
        }

        // Rule #4: Limit Orders Only for Adding to Positions
        if (rules.limitOrdersOnlyEnabled && isOpening) {
            val limitOrderViolation = checkLimitOrderRequirement(trade)
            if (limitOrderViolation != null) {
                violations.add(limitOrderViolation)
            }
        }

        // Rule #5: Max Open Tickers
        if (rules.maxOpenTickersEnabled && isOpening) {
            val maxTickersViolation = checkMaxOpenTickers(rules, trade)
            if (maxTickersViolation != null) {
                violations.add(maxTickersViolation)
            }
        }

        // Return result
        return if (violations.isEmpty()) {
            TradeValidationResult(isValid = true)
        } else {
            TradeValidationResult(
                isValid = false,
                violations = violations,
                errorMessage = formatViolations(violations)
            )
        }
    }

    // Check Rule #2: Daily Loss Limit
    private suspend fun checkDailyLossLimit(
        rules: TradingRules,
        trade: TradeRequest
    ): RuleViolation? {
        val dailyPnL = positionsDataStore.getDailyPnL()

        // Check if already exceeded (should be in liquidation-only mode)
        if (dailyPnL.totalPnL < -rules.dailyLossLimit) {
            return RuleViolation.DailyLossLimitExceeded(
                limit = rules.dailyLossLimit,
                currentLoss = -dailyPnL.totalPnL
            )
        }

        return null
    }

    // Check Rule #3: Max Dollar Per Trade
    private suspend fun checkMaxDollarPerTrade(
        rules: TradingRules,
        trade: TradeRequest
    ): RuleViolation? {
        val tradeValue = kotlin.math.abs(trade.quantity) * trade.price

        if (tradeValue > rules.maxDollarPerTrade) {
            val maxShares = (rules.maxDollarPerTrade / trade.price).toInt()
            return RuleViolation.MaxDollarPerTradeExceeded(
                limit = rules.maxDollarPerTrade,
                tradeValue = tradeValue,
                maxShares = maxShares
            )
        }

        return null
    }

    // Check Rule #4: Limit Orders for Adding to Positions
    private suspend fun checkLimitOrderRequirement(
        trade: TradeRequest
    ): RuleViolation? {
        // Only applies if adding to existing position
        val hasExistingPosition = positionsDataStore.hasPosition(trade.symbol)

        if (hasExistingPosition && trade.orderType == OrderType.MARKET) {
            return RuleViolation.LimitOrderRequired(trade.symbol)
        }

        return null
    }

    // Check Rule #5: Max Open Tickers
    private suspend fun checkMaxOpenTickers(
        rules: TradingRules,
        trade: TradeRequest
    ): RuleViolation? {
        val currentCount = positionsDataStore.getOpenTickersCount()
        val hasExistingPosition = positionsDataStore.hasPosition(trade.symbol)

        // Only check if opening a NEW ticker (not adding to existing)
        if (!hasExistingPosition && currentCount >= rules.maxOpenTickers) {
            return RuleViolation.MaxTickersExceeded(
                limit = rules.maxOpenTickers,
                currentCount = currentCount
            )
        }

        return null
    }

    // Check if currently in liquidation-only mode
    suspend fun isLiquidationOnlyMode(): Boolean {
        val rules = rulesDataStore.getRules()
        if (!rules.dailyLossLimitEnabled) return false

        val dailyPnL = positionsDataStore.getDailyPnL()
        return dailyPnL.totalPnL < -rules.dailyLossLimit
    }

    // Get current daily P&L for display
    suspend fun getCurrentDailyPnL(): DailyPnL {
        return positionsDataStore.getDailyPnL()
    }

    // Format violations into user-friendly message
    private fun formatViolations(violations: List<RuleViolation>): String {
        return violations.joinToString("\n\n") { violation ->
            when (violation) {
                is RuleViolation.DailyLossLimitExceeded -> {
                    "❌ Daily Loss Limit Exceeded\n" +
                            "Limit: -$${String.format("%.2f", violation.limit)}\n" +
                            "Current Loss: -$${String.format("%.2f", violation.currentLoss)}\n" +
                            "Only liquidations allowed today."
                }

                is RuleViolation.MaxDollarPerTradeExceeded -> {
                    "❌ Position Size Too Large\n" +
                            "Max per trade: $${String.format("%.2f", violation.limit)}\n" +
                            "Your trade: $${String.format("%.2f", violation.tradeValue)}\n" +
                            "Max shares: ${violation.maxShares}"
                }

                is RuleViolation.LimitOrderRequired -> {
                    "❌ Limit Order Required\n" +
                            "You have an existing ${violation.symbol} position.\n" +
                            "Adding to positions requires LIMIT orders."
                }

                is RuleViolation.MaxTickersExceeded -> {
                    "❌ Ticker Limit Reached\n" +
                            "Max tickers: ${violation.limit}\n" +
                            "Currently open: ${violation.currentCount}\n" +
                            "Close a position to open a new ticker."
                }
            }
        }
    }

    // Determine if a trade is closing vs opening
    suspend fun isClosingTrade(symbol: String, quantity: Int): Boolean {
        val position = positionsDataStore.getPosition(symbol) ?: return false

        // Closing if opposite direction and reduces position
        return when {
            position.isLong && quantity < 0 -> true  // Sell when long
            position.isShort && quantity > 0 -> true  // Buy to cover when short
            else -> false
        }
    }

    // Get suggested action based on current state
    suspend fun getSuggestedAction(symbol: String): String {
        val position = positionsDataStore.getPosition(symbol)
        val isLiquidationMode = isLiquidationOnlyMode()

        return when {
            isLiquidationMode && position != null -> {
                if (position.isLong) {
                    "Liquidation-only mode: You can SELL ${position.quantity} shares"
                } else {
                    "Liquidation-only mode: You can BUY TO COVER ${kotlin.math.abs(position.quantity)} shares"
                }
            }
            isLiquidationMode && position == null -> {
                "Liquidation-only mode: Cannot open new positions"
            }
            position != null -> {
                if (position.isLong) {
                    "Existing long position: ${position.quantity} shares\nUse LIMIT orders to add"
                } else {
                    "Existing short position: ${kotlin.math.abs(position.quantity)} shares\nUse LIMIT orders to add"
                }
            }
            else -> {
                "No existing position"
            }
        }
    }
}