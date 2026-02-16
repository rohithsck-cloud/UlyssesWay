package com.example.ulyssesway.ui

// Define each main page in your app
sealed class Screen(val route: String, val title: String) {
    object Accounts : Screen("accounts", "Accounts")
    object AccountDetail : Screen("account_detail", "Account Detail")
    object Watchlist : Screen("watchlist", "Watchlist")
    object StockDetail : Screen("stock_detail", "Stock Detail")
    object Trade : Screen("trade", "Trade")
    object Markets : Screen("markets", "Markets")


    // Drawer navigation items
    object Profile : Screen("profile", "Profile")
    object MessageCenter : Screen("message_center", "Message Center")
    object Rules : Screen("rules", "Rules")
}
