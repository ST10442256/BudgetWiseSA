package com.example.budgetwisesa.ui.budgets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.budgetwisesa.R
import com.example.budgetwisesa.data.model.Budget
import com.example.budgetwisesa.viewmodel.MainViewModel

/**
 * Screen for managing and tracking budgets by category.
 * Features: Smart Alerts (80% warning, 100%+ danger), period selection, and remaining balance tracking.
 */
@Composable
fun BudgetsScreen(viewModel: MainViewModel) {
    val budgets by viewModel.budgets.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingBudget by remember { mutableStateOf<Budget?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Default.Add, contentDescription = "Add Budget")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text(text = stringResource(R.string.budgets), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            // Display empty state if no budgets are configured
            if (budgets.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No budgets set. Create one to track spending!", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(budgets, key = { it.id }) { budget ->
                        BudgetItem(
                            budget = budget,
                            onEdit = { editingBudget = it },
                            onDelete = { viewModel.deleteBudget(it) }
                        )
                    }
                }
            }
        }
    }

    // --- Create/Edit Dialogs ---

    if (showAddDialog) {
        AddEditBudgetDialog(
            onDismiss = { showAddDialog = false },
            onSave = { budget ->
                viewModel.addBudget(budget)
                showAddDialog = false
            }
        )
    }

    if (editingBudget != null) {
        AddEditBudgetDialog(
            budget = editingBudget,
            onDismiss = { editingBudget = null },
            onSave = { updated ->
                viewModel.updateBudget(updated)
                editingBudget = null
            }
        )
    }
}

/**
 * Visual card representing a category budget.
 * Automatically calculates progress and displays smart alerts based on spending thresholds.
 */
@Composable
fun BudgetItem(budget: Budget, onEdit: (Budget) -> Unit, onDelete: (Budget) -> Unit) {
    val progress = if (budget.amount > 0) (budget.spent / budget.amount).toFloat() else 0f
    
    // Logic for "Smart Budget Alerts" requirement
    val isNearLimit = progress >= 0.8f && progress <= 1.0f
    val isOverLimit = progress > 1.0f
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = budget.category, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(text = "Period: ${budget.period}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Visual warning indicators
                    if (isOverLimit) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    } else if (isNearLimit) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFFA000))
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(text = { Text("Edit") }, onClick = { showMenu = false; onEdit(budget) })
                            DropdownMenuItem(text = { Text("Delete") }, onClick = { showMenu = false; onDelete(budget) })
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Monetary breakdown
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Spent: R ${String.format("%.2f", budget.spent)}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Remaining: R ${String.format("%.2f", (budget.amount - budget.spent).coerceAtLeast(0.0))}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Threshold-based color progress indicator
            LinearProgressIndicator(
                progress = { progress.coerceAtMost(1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = when {
                    isOverLimit -> MaterialTheme.colorScheme.error
                    isNearLimit -> Color(0xFFFFA000)
                    else -> MaterialTheme.colorScheme.primary
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            // Explanatory alert text
            if (isOverLimit) {
                Text(
                    text = "Smart Alert: Budget Exceeded by R ${String.format("%.2f", budget.spent - budget.amount)}!",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else if (isNearLimit) {
                Text(
                    text = "Smart Alert: You've reached ${(progress * 100).toInt()}% of your budget.",
                    color = Color(0xFFFFA000),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

/**
 * Dialog for defining budget parameters including category, amount, and recurrence period.
 */
@Composable
fun AddEditBudgetDialog(budget: Budget? = null, onDismiss: () -> Unit, onSave: (Budget) -> Unit) {
    var amount by remember { mutableStateOf(budget?.amount?.toString() ?: "") }
    var category by remember { mutableStateOf(budget?.category ?: "") }
    var period by remember { mutableStateOf(budget?.period ?: "Monthly") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (budget == null) "Create New Budget" else "Edit Budget") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text(stringResource(R.string.category)) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text(stringResource(R.string.amount)) }, modifier = Modifier.fillMaxWidth())
                
                Box {
                    OutlinedTextField(
                        value = period,
                        onValueChange = {},
                        label = { Text("Period") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = { IconButton(onClick = { expanded = true }) { Icon(Icons.Default.ArrowDropDown, null) } }
                    )
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(text = { Text("Weekly") }, onClick = { period = "Weekly"; expanded = false })
                        DropdownMenuItem(text = { Text("Monthly") }, onClick = { period = "Monthly"; expanded = false })
                        DropdownMenuItem(text = { Text("Yearly") }, onClick = { period = "Yearly"; expanded = false })
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(Budget(
                    id = budget?.id ?: 0,
                    category = category,
                    amount = amount.toDoubleOrNull() ?: 0.0,
                    spent = budget?.spent ?: 0.0,
                    period = period
                ))
            }) {
                Text(if (budget == null) "Save" else "Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
