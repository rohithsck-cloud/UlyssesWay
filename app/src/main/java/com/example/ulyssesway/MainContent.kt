package com.example.ulyssesway.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import androidx.navigation.NavType
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Colors
    val aegeanBlue = Color(0xFF6FA8C8)

    val items = listOf(
        Screen.Accounts,
        Screen.Watchlist,
        Screen.Trade,
        Screen.Markets
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                onItemClick = { route ->
                    scope.launch {
                        drawerState.close()
                        navController.navigate(route)
                    }
                },
                aegeanBlue = aegeanBlue
            )
        }
    ) {
        Scaffold(
            topBar = {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Only show top bar on main screens (not on detail screens)
                if (currentRoute in listOf(
                        Screen.Accounts.route,
                        Screen.Watchlist.route,
                        Screen.Trade.route,
                        "trade?symbol={symbol}",  // Include trade with parameter
                        Screen.Markets.route
                    )) {
                    TopAppBar(
                        title = {
                            Text(
                                "Ulysses Way",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                scope.launch {
                                    drawerState.open()
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menu",
                                    tint = Color.White
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = aegeanBlue,
                            titleContentColor = Color.White
                        )
                    )
                }
            },
            bottomBar = {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Only show bottom nav on main screens
                if (currentRoute in listOf(
                        Screen.Accounts.route,
                        Screen.Watchlist.route,
                        Screen.Trade.route,
                        "trade?symbol={symbol}",  // Include trade with parameter
                        Screen.Markets.route
                    )) {
                    NavigationBar {
                        items.forEach { screen ->
                            NavigationBarItem(
                                icon = {
                                    when(screen) {
                                        Screen.Accounts -> Icon(Icons.Default.AccountCircle, contentDescription = null)
                                        Screen.Watchlist -> Icon(Icons.Default.List, contentDescription = null)
                                        Screen.Trade -> Icon(Icons.Default.ShoppingCart, contentDescription = null)
                                        Screen.Markets -> Icon(Icons.Default.Info, contentDescription = null)
                                        else -> {}
                                    }
                                },
                                label = { Text(screen.title) },
                                selected = currentRoute == screen.route || currentRoute == "trade?symbol={symbol}",
                                onClick = {
                                    navController.navigate(screen.route) {
                                        // Avoid multiple copies of the same destination
                                        popUpTo(navController.graph.startDestinationId)
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Accounts.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                // Account Screen
                composable(Screen.Accounts.route) { AccountScreen(navController) }

                // Account Detail Screen
                composable(Screen.AccountDetail.route) { AccountDetailScreen(navController) }

                // Watchlist Screen
                composable(Screen.Watchlist.route) { WatchlistScreen(navController) }

                // Stock Detail Screen
                composable(
                    route = "${Screen.StockDetail.route}/{symbol}",
                    arguments = listOf(navArgument("symbol") { type = NavType.StringType })
                ) { backStackEntry ->
                    val symbol = backStackEntry.arguments?.getString("symbol") ?: ""
                    StockDetailScreen(navController, symbol)
                }

                // Trade Screen - with optional symbol parameter
                composable(
                    route = "${Screen.Trade.route}?symbol={symbol}",
                    arguments = listOf(navArgument("symbol") {
                        type = NavType.StringType
                        defaultValue = "AAPL"
                    })
                ) { backStackEntry ->
                    val symbol = backStackEntry.arguments?.getString("symbol") ?: "AAPL"
                    TradeScreen(prefilledSymbol = symbol)
                }

                // Markets Screen
                composable(Screen.Markets.route) { MarketsScreen() }

                // Drawer Screens
                composable(Screen.Profile.route) { ProfileScreen(navController) }
                composable(Screen.MessageCenter.route) { MessageCenterScreen(navController) }
                composable(Screen.Rules.route) { RulesScreen(navController) }
            }
        }
    }
}

@Composable
fun DrawerContent(
    onItemClick: (String) -> Unit,
    aegeanBlue: Color
) {
    ModalDrawerSheet(
        drawerContainerColor = Color.White,
        modifier = Modifier.widthIn(max = 280.dp)
    ) {
        // Drawer Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(aegeanBlue)
                .padding(24.dp)
        ) {
            Column {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Ulysses Way",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = "Trading Platform",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.8f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Drawer Items
        DrawerItem(
            icon = Icons.Default.Person,
            text = "Profile",
            onClick = { onItemClick(Screen.Profile.route) }
        )

        DrawerItem(
            icon = Icons.Default.Email,
            text = "Message Center",
            onClick = { onItemClick(Screen.MessageCenter.route) }
        )

        DrawerItem(
            icon = Icons.Default.Settings,
            text = "Rules",
            onClick = { onItemClick(Screen.Rules.route) }
        )

        Spacer(modifier = Modifier.weight(1f))

        HorizontalDivider()

        // Logout option
        DrawerItem(
            icon = Icons.Default.ExitToApp,
            text = "Logout",
            onClick = { /* TODO: Handle logout */ }
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun DrawerItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = Color(0xFF333333),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Color(0xFF333333)
            )
        )
    }
}

// Placeholder screens for drawer items
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val aegeanBlue = Color(0xFF6FA8C8)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {
        TopAppBar(
            title = { Text("Profile") },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = aegeanBlue,
                modifier = Modifier.size(100.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Profile",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Profile settings and information",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.Gray
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageCenterScreen(navController: NavController) {
    val aegeanBlue = Color(0xFF6FA8C8)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {
        TopAppBar(
            title = { Text("Message Center") },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = null,
                tint = aegeanBlue,
                modifier = Modifier.size(100.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Message Center",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No new messages",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.Gray
                )
            )
        }
    }
}

@Composable
fun PlaceholderScreen(name: String) {
    Text(
        text = "$name Page",
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.padding(16.dp)
    )
}