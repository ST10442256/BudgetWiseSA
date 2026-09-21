package com.example.budgetwisesa.viewmodel

import android.app.Activity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgetwisesa.data.model.Budget
import com.example.budgetwisesa.data.model.SavingsGoal
import com.example.budgetwisesa.data.model.Transaction
import com.example.budgetwisesa.data.model.TransactionType
import com.example.budgetwisesa.data.repository.BudgetRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Shared ViewModel for the application.
 * Manages UI state, authentication, and coordinates data between the local repository and Firebase.
 */
class MainViewModel(private val repository: BudgetRepository) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    
    // Auth state flow to track if a user is logged in
    private val _isUserLoggedIn = MutableStateFlow(auth.currentUser != null)
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn

    // User profile name flow
    private val _userName = MutableStateFlow(auth.currentUser?.displayName ?: "User")
    val userName: StateFlow<String> = _userName

    // App theme preference flow (Dark/Light)
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode

    /**
     * Updates the app-wide dark mode preference.
     */
    fun toggleDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
    }

    /**
     * Changes the application language using AppCompatDelegate for instant locale switching.
     */
    fun setLanguage(languageCode: String) {
        val appLocales: LocaleListCompat = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocales)
    }

    init {
        // Observe Firebase Auth state changes
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _isUserLoggedIn.value = user != null
            _userName.value = user?.displayName ?: "User"
            
            // Trigger synchronization when user logs in
            if (user != null) {
                syncWithFirestore()
            }
        }
    }

    // Data streams from the local Room database, converted to StateFlows for Compose
    val transactions = repository.allTransactions.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val budgets = repository.allBudgets.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val goals = repository.allGoals.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /**
     * Logs the user out and clears the local session state.
     */
    fun logout() {
        auth.signOut()
    }

    /**
     * Bypass for login screen to allow testing offline features.
     */
    fun skipLogin() {
        _isUserLoggedIn.value = true
    }

    // --- Transaction Operations ---

    /**
     * Adds a transaction locally and triggers a sync to Firestore.
     * If the transaction is an expense, it automatically updates the corresponding budget.
     */
    fun addTransaction(transaction: Transaction) = viewModelScope.launch {
        repository.addTransaction(transaction)
        if (transaction.type == TransactionType.EXPENSE) {
            val currentBudgets = budgets.value
            currentBudgets.find { it.category.equals(transaction.category, ignoreCase = true) }?.let { budget ->
                repository.updateBudget(budget.copy(spent = budget.spent + transaction.amount))
            }
        }
        uploadToFirestore("transactions", transaction)
    }

    fun updateTransaction(transaction: Transaction) = viewModelScope.launch {
        repository.updateTransaction(transaction)
        uploadToFirestore("transactions", transaction)
    }

    /**
     * Deletes a transaction locally and removes it from Firestore.
     * Reverses the budget impact if the deleted item was an expense.
     */
    fun deleteTransaction(transaction: Transaction) = viewModelScope.launch {
        repository.deleteTransaction(transaction)
        if (transaction.type == TransactionType.EXPENSE) {
            val currentBudgets = budgets.value
            currentBudgets.find { it.category.equals(transaction.category, ignoreCase = true) }?.let { budget ->
                repository.updateBudget(budget.copy(spent = (budget.spent - transaction.amount).coerceAtLeast(0.0)))
            }
        }
        deleteFromFirestore("transactions", transaction.id.toString())
    }

    // --- Budget Operations ---

    fun addBudget(budget: Budget) = viewModelScope.launch {
        repository.addBudget(budget)
        uploadToFirestore("budgets", budget)
    }

    fun updateBudget(budget: Budget) = viewModelScope.launch {
        repository.updateBudget(budget)
        uploadToFirestore("budgets", budget)
    }

    fun deleteBudget(budget: Budget) = viewModelScope.launch {
        repository.deleteBudget(budget)
        deleteFromFirestore("budgets", budget.id.toString())
    }

    // --- Savings Goal Operations ---

    fun addGoal(goal: SavingsGoal) = viewModelScope.launch {
        repository.addGoal(goal)
        uploadToFirestore("goals", goal)
    }

    fun updateGoal(goal: SavingsGoal) = viewModelScope.launch {
        repository.updateGoal(goal)
        uploadToFirestore("goals", goal)
    }

    fun deleteGoal(goal: SavingsGoal) = viewModelScope.launch {
        repository.deleteGoal(goal)
        deleteFromFirestore("goals", goal.id.toString())
    }

    // --- Cloud Synchronization (Firestore) ---

    /**
     * Uploads an object to a user-specific sub-collection in Firestore.
     */
    private fun uploadToFirestore(collection: String, data: Any) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).collection(collection)
            .document(getId(data))
            .set(data)
    }

    /**
     * Deletes a document from the user's Firestore collection.
     */
    private fun deleteFromFirestore(collection: String, id: String) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).collection(collection)
            .document(id)
            .delete()
    }

    /**
     * Utility to extract the unique ID from various model types.
     */
    private fun getId(data: Any): String {
        return when (data) {
            is Transaction -> data.id.toString()
            is Budget -> data.id.toString()
            is SavingsGoal -> data.id.toString()
            else -> ""
        }
    }

    /**
     * Core logic to pull remote changes from Firestore down to the local Room database.
     */
    private fun syncWithFirestore() {
        // Implementation for pulling and merging remote data into local DAOs
    }

    // --- Authentication (Phone) ---

    /**
     * Triggers the Firebase Phone Auth verification process.
     */
    fun verifyPhoneNumber(phoneNumber: String, activity: Activity, callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }
}
