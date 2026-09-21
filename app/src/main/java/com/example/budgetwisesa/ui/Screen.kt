package com.example.budgetwisesa.ui

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Dashboard : Screen("dashboard")
    object Transactions : Screen("transactions")
    object Budgets : Screen("budgets")
    object Goals : Screen("goals")
    object Reports : Screen("reports")
    object Settings : Screen("settings")
}
