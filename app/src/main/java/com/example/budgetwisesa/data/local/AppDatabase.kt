package com.example.budgetwisesa.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.budgetwisesa.data.model.Budget
import com.example.budgetwisesa.data.model.SavingsGoal
import com.example.budgetwisesa.data.model.Transaction

@Database(entities = [Transaction::class, Budget::class, SavingsGoal::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun savingsGoalDao(): SavingsGoalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budget_wise_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
