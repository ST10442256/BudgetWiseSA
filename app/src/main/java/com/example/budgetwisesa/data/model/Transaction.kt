package com.example.budgetwisesa.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: TransactionType,
    val category: String,
    val amount: Double,
    val date: Long, // Using Long for simplicity with Room/JSON
    val description: String,
    val receiptPath: String? = null,
    val isSynced: Boolean = false
)

enum class TransactionType {
    INCOME, EXPENSE
}
