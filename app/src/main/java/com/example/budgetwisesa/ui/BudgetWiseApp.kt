package com.example.budgetwisesa.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.budgetwisesa.R
import com.example.budgetwisesa.ui.auth.LoginScreen
import com.example.budgetwisesa.ui.auth.RegisterScreen
import com.example.budgetwisesa.ui.dashboard.DashboardScreen
import com.example.budgetwisesa.ui.transactions.TransactionsScreen
import com.example.budgetwisesa.ui.budgets.BudgetsScreen
import com.example.budgetwisesa.ui.goals.GoalsScreen
import com.example.budgetwisesa.ui.reports.ReportsScreen
import com.example.budgetwisesa.ui.settings.SettingsScreen
import com.example.budgetwisesa.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetWiseApp(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsState()

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        bottomBar = {
            if (isUserLoggedIn && currentRoute != Screen.Login.route && currentRoute != Screen.Register.route) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                        label = { Text(stringResource(R.string.dashboard)) },
                        selected = currentRoute == Screen.Dashboard.route,
                        onClick = { navController.navigate(Screen.Dashboard.route) }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null) },
                        label = { Text(stringResource(R.string.transactions)) },
                        selected = currentRoute == Screen.Transactions.route,
                        onClick = { navController.navigate(Screen.Transactions.route) }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null) },
                        label = { Text(stringResource(R.string.budgets)) },
                        selected = currentRoute == Screen.Budgets.route,
                        onClick = { navController.navigate(Screen.Budgets.route) }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Flag, contentDescription = null) },
                        label = { Text(stringResource(R.string.goals)) },
                        selected = currentRoute == Screen.Goals.route,
                        onClick = { navController.navigate(Screen.Goals.route) }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                        label = { Text(stringResource(R.string.reports)) },
                        selected = currentRoute == Screen.Reports.route,
                        onClick = { navController.navigate(Screen.Reports.route) }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        label = { Text(stringResource(R.string.settings)) },
                        selected = currentRoute == Screen.Settings.route,
                        onClick = { navController.navigate(Screen.Settings.route) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (isUserLoggedIn) Screen.Dashboard.route else Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = { 
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    },
                    onSkipLogin = {
                        viewModel.skipLogin()
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    viewModel = viewModel
                )
            }
            composable(Screen.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    },
                    onBackToLogin = {
                        navController.navigate(Screen.Login.route)
                    }
                )
            }
            composable(Screen.Dashboard.route) {
                DashboardScreen(viewModel)
            }
            composable(Screen.Transactions.route) {
                TransactionsScreen(viewModel)
            }
            composable(Screen.Budgets.route) {
                BudgetsScreen(viewModel)
            }
            composable(Screen.Goals.route) {
                GoalsScreen(viewModel)
            }
            composable(Screen.Reports.route) {
                ReportsScreen(viewModel)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onLogout = { 
                        viewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0)
                        }
                    },
                    viewModel = viewModel
                )
            }
        }
    }
}
