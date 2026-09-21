package com.example.budgetwisesa.ui.goals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.budgetwisesa.R
import com.example.budgetwisesa.data.model.SavingsGoal
import com.example.budgetwisesa.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Screen for tracking personal savings objectives.
 * Features: Progress tracking, target dates, and detailed descriptions.
 */
@Composable
fun GoalsScreen(viewModel: MainViewModel) {
    val goals by viewModel.goals.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingGoal by remember { mutableStateOf<SavingsGoal?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Default.Add, contentDescription = "Add Goal")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text(text = stringResource(R.string.goals), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            if (goals.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No goals set. What are you saving for?", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(goals, key = { it.id }) { goal ->
                        GoalItem(
                            goal = goal,
                            onEdit = { editingGoal = it },
                            onDelete = { viewModel.deleteGoal(it) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddEditGoalDialog(
            onDismiss = { showAddDialog = false },
            onSave = { goal ->
                viewModel.addGoal(goal)
                showAddDialog = false
            }
        )
    }

    if (editingGoal != null) {
        AddEditGoalDialog(
            goal = editingGoal,
            onDismiss = { editingGoal = null },
            onSave = { updated ->
                viewModel.updateGoal(updated)
                editingGoal = null
            }
        )
    }
}

/**
 * Component representing a single savings goal with a visual progress bar.
 */
@Composable
fun GoalItem(goal: SavingsGoal, onEdit: (SavingsGoal) -> Unit, onDelete: (SavingsGoal) -> Unit) {
    val progress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat() else 0f
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Flag, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = goal.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "${(progress * 100).toInt()}%", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(text = { Text("Edit") }, onClick = { showMenu = false; onEdit(goal) })
                            DropdownMenuItem(text = { Text("Delete") }, onClick = { showMenu = false; onDelete(goal) })
                        }
                    }
                }
            }
            
            Text(text = "Target: R ${String.format("%.2f", goal.targetAmount)}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Text(text = "Deadline: ${dateFormat.format(Date(goal.targetDate))}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Progress towards the financial goal
            LinearProgressIndicator(
                progress = { progress.coerceAtMost(1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "R ${String.format("%.2f", goal.currentAmount)} of R ${String.format("%.2f", goal.targetAmount)} saved",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Dialog for capturing goal details including target amounts and current progress.
 */
@Composable
fun AddEditGoalDialog(goal: SavingsGoal? = null, onDismiss: () -> Unit, onSave: (SavingsGoal) -> Unit) {
    var name by remember { mutableStateOf(goal?.name ?: "") }
    var targetAmount by remember { mutableStateOf(goal?.targetAmount?.toString() ?: "") }
    var currentAmount by remember { mutableStateOf(goal?.currentAmount?.toString() ?: "0.0") }
    var description by remember { mutableStateOf(goal?.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (goal == null) "Set New Savings Goal" else "Edit Savings Goal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Goal Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = targetAmount, onValueChange = { targetAmount = it }, label = { Text("Target Amount") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = currentAmount, onValueChange = { currentAmount = it }, label = { Text("Already Saved") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(SavingsGoal(
                    id = goal?.id ?: 0,
                    name = name,
                    targetAmount = targetAmount.toDoubleOrNull() ?: 0.0,
                    currentAmount = currentAmount.toDoubleOrNull() ?: 0.0,
                    targetDate = goal?.targetDate ?: (System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000),
                    description = description
                ))
            }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
