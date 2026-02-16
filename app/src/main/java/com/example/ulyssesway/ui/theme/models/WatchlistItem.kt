package com.example.ulyssesway.ui.models

data class WatchlistItem(
    val symbol: String,
    val name: String,
    val price: Double,
    val change: Double,
    val percentChange: Double
)
