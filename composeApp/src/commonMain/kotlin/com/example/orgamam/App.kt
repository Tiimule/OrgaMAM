package com.example.orgamam

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.orgamam.service.UpdateManager
import com.example.orgamam.service.openUrl
import com.example.orgamam.ui.auth.AuthViewModel
import com.example.orgamam.ui.auth.LoginScreen
import com.example.orgamam.ui.calendar.CalendarScreen
import com.example.orgamam.ui.whatsnew.WhatsNewScreen

@Composable
fun App() {
    MaterialTheme {
        val authViewModel: AuthViewModel = viewModel { AuthViewModel() }
        val authState by authViewModel.uiState.collectAsState()
        
        val updateManager = remember { UpdateManager() }
        val updateInfo by updateManager.updateState.collectAsState()
        var showWhatsNew by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            updateManager.checkUpdates()
        }

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                if (showWhatsNew && updateInfo != null) {
                    WhatsNewScreen(content = updateInfo!!.whatsNew, onDismiss = { showWhatsNew = false })
                } else {
                    when {
                        authState.isLoggedOut -> {
                            LoginScreen(authViewModel)
                        }
                        authState.user != null && authState.user?.groupId.isNullOrEmpty() -> {
                            LoginScreen(authViewModel)
                        }
                        authState.user != null -> {
                            CalendarScreen(authViewModel = authViewModel)
                        }
                        else -> {
                            // Loading...
                        }
                    }
                }
            }
        }

        updateInfo?.let { info ->
            AlertDialog(
                onDismissRequest = { /* Force update if needed, or allow dismiss */ },
                title = { Text("Mise à jour disponible") },
                text = { Text("Une nouvelle version d'OrgaMAM est disponible. Voulez-vous l'installer ?") },
                confirmButton = {
                    Button(onClick = { 
                        openUrl(info.apkUrl)
                    }) { Text("Mettre à jour") }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        showWhatsNew = true 
                        // Note: Normally we'd only show this after actual update, 
                        // but here we can use it to preview changes.
                    }) { Text("Voir les nouveautés") }
                }
            )
        }
    }
}
