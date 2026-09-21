package com.example.budgetwisesa.data.repository

import com.example.budgetwisesa.data.local.BudgetDao
import com.example.budgetwisesa.data.local.SavingsGoalDao
import com.example.budgetwisesa.data.local.TransactionDao
import com.example.budgetwisesa.data.model.Budget
import com.example.budgetwisesa.data.model.SavingsGoal
import com.example.budgetwisesa.data.model.Transaction
import com.example.budgetwisesa.data.remote.ApiService
import kotlinx.coroutines.flow.Flow

class BudgetRepository(
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao,
    private val savingsGoalDao: SavingsGoalDao,
    private val apiService: ApiService
) {
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    val allBudgets: Flow<List<Budget>> = budgetDao.getAllBudgets()
    val allGoals: Flow<List<SavingsGoal>> = savingsGoalDao.getAllGoals()

    suspend fun addTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
    }
    
    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }
    
    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun addBudget(budget: Budget) {
        budgetDao.insertBudget(budget)
    }
    
    suspend fun updateBudget(budget: Budget) {
        budgetDao.updateBudget(budget)
    }
    
    suspend fun deleteBudget(budget: Budget) {
        budgetDao.deleteBudget(budget)
    }

    suspend fun addGoal(goal: SavingsGoal) {
        savingsGoalDao.insertGoal(goal)
    }
    
    suspend fun updateGoal(goal: SavingsGoal) {
        savingsGoalDao.updateGoal(goal)
    }
    
    suspend fun deleteGoal(goal: SavingsGoal) {
        savingsGoalDao.deleteGoal(goal)
    }
}
