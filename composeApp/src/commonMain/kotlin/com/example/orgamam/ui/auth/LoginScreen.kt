package com.example.orgamam.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LoginScreen(viewModel: AuthViewModel = viewModel { AuthViewModel() }) {
    val uiState by viewModel.uiState.collectAsState()
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    var groupId by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isSignUp) "Créer un compte" else "Connexion",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        if (isSignUp) {
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Nom d'utilisateur") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mot de passe") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (uiState.user != null && uiState.user?.groupId.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = groupId,
                onValueChange = { groupId = it },
                label = { Text("Code de groupe (ex: FAMILLE1)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            
            Button(
                onClick = { viewModel.joinGroup(groupId) },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text("Rejoindre le groupe")
            }
        } else {
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    if (isSignUp) viewModel.signUp(email, password, displayName)
                    else viewModel.signIn(email, password)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                else Text(if (isSignUp) "S'inscrire" else "Se connecter")
            }
            
            TextButton(onClick = { isSignUp = !isSignUp }) {
                Text(if (isSignUp) "Déjà un compte ? Se connecter" else "Pas de compte ? S'inscrire")
            }
        }

        uiState.error?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }
    }
}
