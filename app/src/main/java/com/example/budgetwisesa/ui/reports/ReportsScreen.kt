package com.example.budgetwisesa.ui.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.budgetwisesa.R
import com.example.budgetwisesa.data.model.TransactionType
import com.example.budgetwisesa.viewmodel.MainViewModel

/**
 * Screen for advanced data visualization and financial analysis.
 * Features: Donut charts, category breakdown, and performance ratios.
 */
@Composable
fun ReportsScreen(viewModel: MainViewModel) {
    val transactions by viewModel.transactions.collectAsState()
    val budgets by viewModel.budgets.collectAsState()
    val goals by viewModel.goals.collectAsState()

    // Aggregate data for visualization
    val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
    val totalExpense = expenses.sumOf { it.amount }
    val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    
    // Group transactions by category for the chart
    val categoryTotals = expenses.groupBy { it.category }.mapValues { it.value.sumOf { t -> t.amount } }
    val chartColors = listOf(Color(0xFF4CAF50), Color(0xFF2196F3), Color(0xFFF44336), Color(0xFFFFEB3B), Color(0xFF9C27B0))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.reports),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // --- Summary Performance Card ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Monthly Performance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                // Calculate budget compliance percentage
                val budgetPerf = if (budgets.isNotEmpty()) {
                    val totalBudget = budgets.sumOf { it.amount }
                    val totalSpent = budgets.sumOf { it.spent }
                    if (totalBudget > 0) "${( (totalBudget - (totalSpent - totalBudget).coerceAtLeast(0.0)) / totalBudget * 100).toInt()}%" else "100%"
                } else "N/A"
                
                // Calculate savings achievement percentage
                val savingsPerf = if (goals.isNotEmpty()) {
                    val totalTarget = goals.sumOf { it.targetAmount }
                    val totalSaved = goals.sumOf { it.currentAmount }
                    if (totalTarget > 0) "${(totalSaved / totalTarget * 100).toInt()}%" else "0%"
                } else "N/A"

                PerformanceRow("Budget Performance", budgetPerf, MaterialTheme.colorScheme.primary)
                PerformanceRow("Savings Progress", savingsPerf, Color(0xFF2196F3))
                PerformanceRow("Expense Ratio", if (totalIncome > 0) "${(totalExpense / totalIncome * 100).toInt()}%" else "0%", MaterialTheme.colorScheme.error)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        Text(text = "Spending by Category", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        if (expenses.isEmpty()) {
            Box(modifier = Modifier.height(220.dp), contentAlignment = Alignment.Center) {
                Text("No data to display", color = Color.Gray)
            }
        } else {
            // --- Custom Dynamic Donut Chart ---
            Box(modifier = Modifier.size(220.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    var startAngle = 0f
                    categoryTotals.entries.forEachIndexed { index, entry ->
                        val sweepAngle = (entry.value / totalExpense * 360f).toFloat()
                        drawArc(
                            color = chartColors[index % chartColors.size],
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = true,
                            size = Size(size.width, size.height)
                        )
                        startAngle += sweepAngle
                    }
                }
                // Overlay inner circle for donut appearance
                Box(modifier = Modifier.size(120.dp).background(MaterialTheme.colorScheme.background, shape = androidx.compose.foundation.shape.CircleShape))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Spent", style = MaterialTheme.typography.labelSmall)
                    Text("R ${String.format("%.2f", totalExpense)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Chart Legend
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                categoryTotals.entries.forEachIndexed { index, entry ->
                    LegendItem(
                        color = chartColors[index % chartColors.size],
                        label = entry.key,
                        value = "R ${String.format("%.2f", entry.value)}"
                    )
                }
            }
        }
    }
}

@Composable
fun PerformanceRow(label: String, value: String, color: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun LegendItem(color: Color, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Canvas(modifier = Modifier.size(16.dp)) {
                drawCircle(color = color)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
        }
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
