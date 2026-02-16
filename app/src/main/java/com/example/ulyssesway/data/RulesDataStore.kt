package com.example.ulyssesway.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.ulyssesway.ui.TradingRules
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first

private val Context.rulesDataStore: DataStore<Preferences> by preferencesDataStore(name = "trading_rules")

class RulesDataStore(private val context: Context) {

    companion object {
        // Keys for each rule
        private val DAILY_LOSS_LIMIT = doublePreferencesKey("daily_loss_limit")
        private val MAX_DOLLAR_PER_TRADE = doublePreferencesKey("max_dollar_per_trade")
        private val MAX_OPEN_TICKERS = intPreferencesKey("max_open_tickers")

        // Keys for enabled/disabled states
        private val DAILY_LOSS_ENABLED = booleanPreferencesKey("daily_loss_enabled")
        private val MAX_DOLLAR_ENABLED = booleanPreferencesKey("max_dollar_enabled")
        private val LIMIT_ORDERS_ENABLED = booleanPreferencesKey("limit_orders_enabled")
        private val MAX_TICKERS_ENABLED = booleanPreferencesKey("max_tickers_enabled")
    }

    // Save all rules
    suspend fun saveRules(rules: TradingRules) {
        context.rulesDataStore.edit { preferences ->
            preferences[DAILY_LOSS_LIMIT] = rules.dailyLossLimit
            preferences[MAX_DOLLAR_PER_TRADE] = rules.maxDollarPerTrade
            preferences[MAX_OPEN_TICKERS] = rules.maxOpenTickers

            preferences[DAILY_LOSS_ENABLED] = rules.dailyLossLimitEnabled
            preferences[MAX_DOLLAR_ENABLED] = rules.maxDollarPerTradeEnabled
            preferences[LIMIT_ORDERS_ENABLED] = rules.limitOrdersOnlyEnabled
            preferences[MAX_TICKERS_ENABLED] = rules.maxOpenTickersEnabled
        }
    }

    // Load all rules as a Flow (reactive)
    val rules: Flow<TradingRules> = context.rulesDataStore.data.map { preferences ->
        TradingRules(
            dailyLossLimit = preferences[DAILY_LOSS_LIMIT] ?: 500.0,
            maxDollarPerTrade = preferences[MAX_DOLLAR_PER_TRADE] ?: 5000.0,
            maxOpenTickers = preferences[MAX_OPEN_TICKERS] ?: 5,

            dailyLossLimitEnabled = preferences[DAILY_LOSS_ENABLED] ?: true,
            maxDollarPerTradeEnabled = preferences[MAX_DOLLAR_ENABLED] ?: true,
            limitOrdersOnlyEnabled = preferences[LIMIT_ORDERS_ENABLED] ?: true,
            maxOpenTickersEnabled = preferences[MAX_TICKERS_ENABLED] ?: true
        )
    }

    // Get rules synchronously (for use in non-composable contexts)
    suspend fun getRules(): TradingRules {
        val preferences = context.rulesDataStore.data.map { it }.first()
        return TradingRules(
            dailyLossLimit = preferences[DAILY_LOSS_LIMIT] ?: 500.0,
            maxDollarPerTrade = preferences[MAX_DOLLAR_PER_TRADE] ?: 5000.0,
            maxOpenTickers = preferences[MAX_OPEN_TICKERS] ?: 5,

            dailyLossLimitEnabled = preferences[DAILY_LOSS_ENABLED] ?: true,
            maxDollarPerTradeEnabled = preferences[MAX_DOLLAR_ENABLED] ?: true,
            limitOrdersOnlyEnabled = preferences[LIMIT_ORDERS_ENABLED] ?: true,
            maxOpenTickersEnabled = preferences[MAX_TICKERS_ENABLED] ?: true
        )
    }

    // Clear all rules (for logout or reset)
    suspend fun clearRules() {
        context.rulesDataStore.edit { preferences ->
            preferences.clear()
        }
    }
}