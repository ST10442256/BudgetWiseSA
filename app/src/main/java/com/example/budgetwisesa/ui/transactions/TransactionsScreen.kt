package com.example.budgetwisesa.ui.transactions

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.budgetwisesa.R
import com.example.budgetwisesa.data.model.Transaction
import com.example.budgetwisesa.data.model.TransactionType
import com.example.budgetwisesa.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Screen for managing and viewing financial transactions.
 * Features: Search, Filter (Income/Expense), Edit/Delete, and Receipt Attachments.
 */
@Composable
fun TransactionsScreen(viewModel: MainViewModel) {
    val transactions by viewModel.transactions.collectAsState()
    
    // UI state for dialogs and filtering
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var viewReceiptUri by remember { mutableStateOf<String?>(null) }
    
    // Combined logic for live searching and filtering
    val filteredTransactions = transactions.filter {
        val matchesSearch = it.category.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            "Income" -> it.type == TransactionType.INCOME
            "Expense" -> it.type == TransactionType.EXPENSE
            else -> true
        }
        matchesSearch && matchesFilter
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_transaction))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text(text = stringResource(R.string.transactions), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search transactions...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = MaterialTheme.shapes.medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Filter Chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = selectedFilter == "All", onClick = { selectedFilter = "All" }, label = { Text("All") })
                FilterChip(selected = selectedFilter == "Income", onClick = { selectedFilter = "Income" }, label = { Text("Income") })
                FilterChip(selected = selectedFilter == "Expense", onClick = { selectedFilter = "Expense" }, label = { Text("Expense") })
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Efficient scrolling list of transactions
            if (filteredTransactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No transactions found", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredTransactions, key = { it.id }) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            onEdit = { editingTransaction = it },
                            onDelete = { viewModel.deleteTransaction(it) },
                            onViewReceipt = { viewReceiptUri = it }
                        )
                    }
                }
            }
        }
    }

    // --- Interaction Dialogs ---

    if (showAddDialog) {
        AddEditTransactionDialog(
            onDismiss = { showAddDialog = false },
            onSave = { transaction ->
                viewModel.addTransaction(transaction)
                showAddDialog = false
            }
        )
    }

    if (editingTransaction != null) {
        AddEditTransactionDialog(
            transaction = editingTransaction,
            onDismiss = { editingTransaction = null },
            onSave = { updated ->
                viewModel.updateTransaction(updated)
                editingTransaction = null
            }
        )
    }

    // Overlay for viewing receipt images
    if (viewReceiptUri != null) {
        ReceiptViewer(uri = viewReceiptUri!!, onDismiss = { viewReceiptUri = null })
    }
}

/**
 * List item for a single transaction.
 * Displays type-specific colors, amounts, and receipt attachment indicators.
 */
@Composable
fun TransactionItem(
    transaction: Transaction, 
    onEdit: (Transaction) -> Unit, 
    onDelete: (Transaction) -> Unit,
    onViewReceipt: (String) -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    // Arrow icons for Income (Up) and Expense (Down)
                    Box(
                        modifier = Modifier.size(40.dp).background(
                            if (transaction.type == TransactionType.INCOME) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            else MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                            shape = MaterialTheme.shapes.small
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (transaction.type == TransactionType.INCOME) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = if (transaction.type == TransactionType.INCOME) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = transaction.category, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(text = dateFormat.format(Date(transaction.date)), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${if (transaction.type == TransactionType.INCOME) "+" else "-"} R ${String.format("%.2f", transaction.amount)}",
                        color = if (transaction.type == TransactionType.INCOME) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    // Overflow menu for Edit/Delete actions
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(text = { Text("Edit") }, onClick = { showMenu = false; onEdit(transaction) })
                            DropdownMenuItem(text = { Text("Delete") }, onClick = { showMenu = false; onDelete(transaction) })
                        }
                    }
                }
            }
            
            // Receipt indicator (clickable to view)
            if (transaction.receiptPath != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onViewReceipt(transaction.receiptPath) }
                        .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View Receipt", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

/**
 * Universal dialog for adding new or editing existing transactions.
 * Handles category selection, amount, date, and receipt attachment.
 */
@Composable
fun AddEditTransactionDialog(transaction: Transaction? = null, onDismiss: () -> Unit, onSave: (Transaction) -> Unit) {
    var amount by remember { mutableStateOf(transaction?.amount?.toString() ?: "") }
    var category by remember { mutableStateOf(transaction?.category ?: "") }
    var description by remember { mutableStateOf(transaction?.description ?: "") }
    var type by remember { mutableStateOf(transaction?.type ?: TransactionType.EXPENSE) }
    var date by remember { mutableStateOf(transaction?.date ?: System.currentTimeMillis()) }
    var receiptUri by remember { mutableStateOf<Uri?>(transaction?.receiptPath?.let { Uri.parse(it) }) }

    // Launcher for selecting images from the device storage
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        receiptUri = uri
    }

    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (transaction == null) "Add Transaction" else "Edit Transaction") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SegmentedControl(selectedType = type, onTypeSelected = { type = it })
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount (R)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                
                OutlinedTextField(
                    value = dateFormat.format(Date(date)),
                    onValueChange = { /* In real app, open date picker */ },
                    label = { Text("Date (dd/mm/yyyy)") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = { IconButton(onClick = { /* Date Picker Logic */ }) { Icon(Icons.Default.DateRange, null) } }
                )
                
                Button(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(if (receiptUri == null) Icons.Default.PhotoCamera else Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (receiptUri == null) "Attach Receipt" else "Receipt Attached")
                }
                
                if (receiptUri != null) {
                    AsyncImage(
                        model = receiptUri,
                        contentDescription = "Receipt Preview",
                        modifier = Modifier.size(100.dp).align(Alignment.CenterHorizontally),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(Transaction(
                    id = transaction?.id ?: 0,
                    type = type,
                    category = category,
                    amount = amount.toDoubleOrNull() ?: 0.0,
                    date = date,
                    description = description,
                    receiptPath = receiptUri?.toString()
                ))
            }) {
                Text(if (transaction == null) "Add" else "Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * Image viewer dialog for attached receipts.
 * Includes a "Download" action as per requirements.
 */
@Composable
fun ReceiptViewer(uri: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().height(550.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Receipt Image", style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
                }
                Spacer(modifier = Modifier.height(16.dp))
                AsyncImage(
                    model = uri,
                    contentDescription = "Receipt",
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { /* Download logic for storage export */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download Receipt")
                }
            }
        }
    }
}

/**
 * Toggle component for choosing between Income and Expense.
 */
@Composable
fun SegmentedControl(selectedType: TransactionType, onTypeSelected: (TransactionType) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = { onTypeSelected(TransactionType.INCOME) },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedType == TransactionType.INCOME) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (selectedType == TransactionType.INCOME) Color.White else Color.Black
            ),
            shape = MaterialTheme.shapes.small
        ) {
            Text("Income")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = { onTypeSelected(TransactionType.EXPENSE) },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedType == TransactionType.EXPENSE) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (selectedType == TransactionType.EXPENSE) Color.White else Color.Black
            ),
            shape = MaterialTheme.shapes.small
        ) {
            Text("Expense")
        }
    }
}
