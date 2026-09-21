package com.example.budgetwisesa.ui.auth

import android.app.Activity
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.budgetwisesa.R
import com.example.budgetwisesa.viewmodel.MainViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

/**
 * Modern Login Screen with support for Email, Phone, and Google Sign-In.
 * Fulfills the "Single Sign-On" and "Multi-method Auth" requirements.
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onSkipLogin: () -> Unit,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    
    // Remember previous login method to improve usability
    val sharedPrefs = remember { context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE) }
    val lastMethod = remember { sharedPrefs.getString("last_method", "Email") ?: "Email" }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    
    // UI state for switching between login types
    var isPhoneLogin by remember { mutableStateOf(lastMethod == "Phone") }
    var isCodeSent by remember { mutableStateOf(false) }
    var verificationId by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val auth = FirebaseAuth.getInstance()
    
    // Handler for Google Sign-In result
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)!!
                val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
                auth.signInWithCredential(credential).addOnCompleteListener {
                    if (it.isSuccessful) {
                        sharedPrefs.edit().putString("last_method", "Google").apply()
                        onLoginSuccess()
                    } else errorMessage = it.exception?.message
                }
            } catch (e: Exception) {
                errorMessage = "Google Sign-In Failed: ${e.message}"
            }
        }
    }

    // Reset error when user interacts with fields
    LaunchedEffect(email, password, phoneNumber) {
        errorMessage = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Logo Icon (Circular Green Wallet from design requirements)
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "R", style = MaterialTheme.typography.displaySmall, color = Color.White, fontWeight = FontWeight.ExtraBold)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "BudgetWise SA", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        
        // Reminder for user convenience
        if (lastMethod != null) {
            Text(text = "Previous method: $lastMethod", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        // --- Authentication Inputs ---
        if (isPhoneLogin) {
            if (!isCodeSent) {
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number (e.g., +27123456789)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    leadingIcon = { Icon(Icons.Default.Phone, null) }
                )
            } else {
                OutlinedTextField(
                    value = verificationCode,
                    onValueChange = { verificationCode = it },
                    label = { Text("Verification Code") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )
            }
        } else {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(R.string.email)) },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.password)) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )
        }
        
        // Display errors to the user
        if (errorMessage != null) {
            Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Primary Sign-In Button
        Button(
            onClick = {
                if (isPhoneLogin) {
                    if (!isCodeSent) {
                        // Phone verification step
                        if (phoneNumber.startsWith("+")) {
                            viewModel.verifyPhoneNumber(phoneNumber, context as Activity, object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                                    auth.signInWithCredential(credential).addOnCompleteListener { 
                                        if (it.isSuccessful) {
                                            sharedPrefs.edit().putString("last_method", "Phone").apply()
                                            onLoginSuccess()
                                        }
                                    }
                                }
                                override fun onVerificationFailed(e: FirebaseException) {
                                    errorMessage = "Phone Auth Failed: ${e.message}"
                                }
                                override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                                    verificationId = id
                                    isCodeSent = true
                                }
                            })
                        } else {
                            errorMessage = "Please include country code (e.g. +27)"
                        }
                    } else {
                        // OTP verification step
                        val credential = PhoneAuthProvider.getCredential(verificationId, verificationCode)
                        auth.signInWithCredential(credential).addOnCompleteListener { 
                            if (it.isSuccessful) {
                                sharedPrefs.edit().putString("last_method", "Phone").apply()
                                onLoginSuccess()
                            } else errorMessage = it.exception?.message
                        }
                    }
                } else {
                    // Standard Email sign-in
                    if (email.isNotEmpty() && password.isNotEmpty()) {
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    sharedPrefs.edit().putString("last_method", "Email").apply()
                                    onLoginSuccess()
                                } else errorMessage = task.exception?.message
                            }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(if (isPhoneLogin && !isCodeSent) "Send Code" else stringResource(R.string.login))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // --- SSO & Options ---
        OutlinedButton(
            onClick = {
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken("787284964151-eor5fhsa06ot8ckth3u8medg2ff101e4.apps.googleusercontent.com")
                    .requestEmail()
                    .build()
                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                // Force show account picker
                googleSignInClient.signOut().addOnCompleteListener {
                    googleSignInLauncher.launch(googleSignInClient.signInIntent)
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(text = "Sign in with Google")
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Switch between auth modes
        TextButton(onClick = { isPhoneLogin = !isPhoneLogin; isCodeSent = false }) {
            Text(if (isPhoneLogin) "Login with Email" else "Login with Phone")
        }
        
        TextButton(onClick = onNavigateToRegister) {
            Text("Don't have an account? Register")
        }
        
        // --- Development/Fallback Options ---
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        TextButton(onClick = onSkipLogin) {
            Text("Use Local Offline Mode", fontWeight = FontWeight.Bold)
        }
    }
}
