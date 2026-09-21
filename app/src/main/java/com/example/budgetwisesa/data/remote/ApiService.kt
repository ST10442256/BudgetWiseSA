package com.example.budgetwisesa.data.remote

import com.example.budgetwisesa.data.model.Budget
import com.example.budgetwisesa.data.model.SavingsGoal
import com.example.budgetwisesa.data.model.Transaction
import retrofit2.http.*

interface ApiService {
    @GET("transactions")
    suspend fun getTransactions(): List<Transaction>

    @POST("transactions")
    suspend fun postTransaction(@Body transaction: Transaction): Transaction

    @GET("budgets")
    suspend fun getBudgets(): List<Budget>

    @POST("budgets")
    suspend fun postBudget(@Body budget: Budget): Budget

    @GET("goals")
    suspend fun getGoals(): List<SavingsGoal>

    @POST("goals")
    suspend fun postGoal(@Body goal: SavingsGoal): SavingsGoal
}
