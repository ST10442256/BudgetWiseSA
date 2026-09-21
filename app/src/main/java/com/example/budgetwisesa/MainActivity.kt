package com.example.budgetwisesa

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import com.example.budgetwisesa.data.local.AppDatabase
import com.example.budgetwisesa.data.remote.ApiService
import com.example.budgetwisesa.data.repository.BudgetRepository
import com.example.budgetwisesa.ui.BudgetWiseApp
import com.example.budgetwisesa.ui.theme.BudgetWiseSATheme
import com.example.budgetwisesa.ui.theme.DarkGray
import com.example.budgetwisesa.ui.theme.WalletGreen
import com.example.budgetwisesa.viewmodel.MainViewModel
import com.example.budgetwisesa.viewmodel.ViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Requirement: After app is closed user should be required to sign in again
        auth.signOut()

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = WalletGreen.toArgb()
        
        val database = AppDatabase.getDatabase(this)
        val retrofit = Retrofit.Builder()
            .baseUrl("https://budgetwise-api.onrender.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val apiService = retrofit.create(ApiService::class.java)
        val repository = BudgetRepository(
            database.transactionDao(),
            database.budgetDao(),
            database.savingsGoalDao(),
            apiService
        )
        val viewModelFactory = ViewModelFactory(repository)
        val viewModel: MainViewModel by viewModels { viewModelFactory }

        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            
            // Sync status bar color with theme
            val statusBarColor = if (isDarkMode) DarkGray else WalletGreen
            window.statusBarColor = statusBarColor.toArgb()

            BudgetWiseSATheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    BudgetWiseApp(viewModel)
                }
            }
        }
    }
}
