package com.example.budgetwisesa.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.budgetwisesa.R
import com.example.budgetwisesa.data.model.Transaction
import com.example.budgetwisesa.data.model.TransactionType
import com.example.budgetwisesa.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * High-level summary of the user's financial health.
 * Displays current balance, gamification badges, and recent transactions.
 */
@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    // Reactive data collection from ViewModel
    val transactions by viewModel.transactions.collectAsState()
    
    // Financial calculations
    val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val currentBalance = totalIncome - totalExpense

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(R.string.dashboard),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // --- Circular Wallet Themed Balance Card ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "R", style = MaterialTheme.typography.headlineLarge, color = Color.White)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Current Balance", color = Color.White.copy(alpha = 0.8f))
                Text(text = "R ${String.format("%.2f", currentBalance)}", style = MaterialTheme.typography.displaySmall, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // --- Gamification: Badges Section ---
        Text(text = stringResource(R.string.badges), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item { BadgeItem("7-Day Budget", Icons.Default.Star, Color(0xFFFFD700)) }
            item { BadgeItem("Goal Master", Icons.Default.TrendingUp, Color(0xFF4CAF50)) }
            item { BadgeItem("Expense Pro", Icons.Default.Star, Color(0xFF2196F3)) }
            item { BadgeItem("3-Month Saver", Icons.Default.Star, Color(0xFFFF9800)) }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Quick Summary Row ---
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SummarySmallCard(stringResource(R.string.income), "R ${String.format("%.2f", totalIncome)}", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
            SummarySmallCard(stringResource(R.string.expense), "R ${String.format("%.2f", totalExpense)}", MaterialTheme.colorScheme.error, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        // --- Activity Feed ---
        Text(text = stringResource(R.string.recent_transactions), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        
        if (transactions.isEmpty()) {
            Text("No transactions yet. Start by adding one!", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        } else {
            // Show only the 5 most recent items
            transactions.take(5).forEach { transaction ->
                TransactionSummaryItem(transaction)
            }
        }
    }
}

/**
 * Reusable badge component for gamification features.
 */
@Composable
fun BadgeItem(name: String, icon: ImageVector, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(32.dp))
        }
        Text(text = name, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun SummarySmallCard(title: String, amount: String, color: Color, modifier: Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelMedium)
            Text(text = amount, style = MaterialTheme.typography.titleLarge, color = color, fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * Compact list item for displaying transaction details on the dashboard.
 */
@Composable
fun TransactionSummaryItem(transaction: Transaction) {
    val dateFormat = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // Emoji icons for quick visual identification
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                Text(if (transaction.type == TransactionType.INCOME) "💰" else "🛒")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.category, fontWeight = FontWeight.Medium)
                Text(dateFormat.format(Date(transaction.date)), style = MaterialTheme.typography.bodySmall)
            }
            Text(
                text = "${if (transaction.type == TransactionType.INCOME) "+" else "-"} R ${String.format("%.2f", transaction.amount)}",
                color = if (transaction.type == TransactionType.INCOME) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
