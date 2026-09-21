package com.example.budgetwisesa.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.budgetwisesa.R
import com.example.budgetwisesa.viewmodel.MainViewModel

/**
 * Screen for managing user preferences and app configuration.
 * Includes settings for Dark Mode, Language, Currency, and Smart Alert Thresholds.
 */
@Composable
fun SettingsScreen(onLogout: () -> Unit, viewModel: MainViewModel) {
    // Reactive state from ViewModel
    val userName by viewModel.userName.collectAsState()
    
    // UI state for local preference toggles
    var currency by remember { mutableStateOf("ZAR (R)") }
    var threshold by remember { mutableStateOf(80f) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(text = stringResource(R.string.settings), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        
        // --- Profile Management Section ---
        SettingsSectionTitle("Profile")
        OutlinedTextField(
            value = userName,
            onValueChange = { /* Placeholder for name editing logic */ },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            readOnly = true // Name is sourced from Firebase Profile
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // --- Personalization Preferences ---
        SettingsSectionTitle("Preferences")
        
        // Dynamic Language Switching (Supports English & Tshivenda)
        LanguageSelector(onLanguageSelected = { code, name ->
            viewModel.setLanguage(code)
        })
        
        SettingsRow("Currency", currency) {
            // Placeholder for currency picker
        }
        
        // App Theme Toggle
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Dark Mode", style = MaterialTheme.typography.bodyLarge)
            Switch(checked = isDarkMode, onCheckedChange = { viewModel.toggleDarkMode(it) })
        }

        // Start of month preference for budget calculations
        var startOfMonth by remember { mutableStateOf("1st") }
        SettingsRow("Start of Month", startOfMonth) {
            // Logic to change start of month
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // --- System Notifications & Smart Alerts ---
        SettingsSectionTitle("Notifications & Smart Alerts")
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Enable Notifications", style = MaterialTheme.typography.bodyLarge)
            Switch(checked = notificationsEnabled, onCheckedChange = { notificationsEnabled = it })
        }
        
        // Custom threshold for the "80%" requirement
        Text(text = "Budget Alert Threshold: ${threshold.toInt()}%", style = MaterialTheme.typography.bodyMedium)
        Slider(
            value = threshold,
            onValueChange = { threshold = it },
            valueRange = 50f..100f,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // --- Manual Sync Controls ---
        SettingsSectionTitle("System")
        Button(
            onClick = { /* Manually trigger Firestore sync */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Icon(Icons.Default.Sync, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sync Transactions Now")
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Application Logout
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text(text = "Logout")
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun SettingsRow(label: String, value: String, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            Text(text = value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}

/**
 * Helper component for picking and applying app languages.
 */
@Composable
fun LanguageSelector(onLanguageSelected: (String, String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf("English") }
    
    Box {
        SettingsRow("App Language", selectedLanguage) { expanded = true }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("English") }, 
                onClick = { 
                    selectedLanguage = "English"
                    expanded = false
                    onLanguageSelected("en", "English")
                }
            )
            DropdownMenuItem(
                text = { Text("Tshivenda") }, 
                onClick = { 
                    selectedLanguage = "Tshivenda"
                    expanded = false
                    onLanguageSelected("ven", "Tshivenda")
                }
            )
        }
    }
}
