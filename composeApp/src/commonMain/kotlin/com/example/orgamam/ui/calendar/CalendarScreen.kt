package com.example.orgamam.ui.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.orgamam.model.CalendarEvent
import com.example.orgamam.ui.auth.AuthViewModel
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.minus
import kotlinx.datetime.plus

fun String.toColor(): Color {
    return try {
        val hex = this.removePrefix("#")
        val color = hex.toLong(16)
        if (hex.length == 6) {
            Color(color or 0x00000000FF000000)
        } else {
            Color(color)
        }
    } catch (e: Exception) {
        Color.Gray
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = viewModel { CalendarViewModel() },
    authViewModel: AuthViewModel = viewModel { AuthViewModel() }
) {
    val uiState by viewModel.uiState.collectAsState()
    val authState by authViewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val filteredEvents = remember(uiState.selectedDate, uiState.events) {
        uiState.events.filter { it.date == uiState.selectedDate }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("OrgaMAM", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Paramètres")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            CalendarHeader(
                currentMonthDate = uiState.currentMonthDate,
                onPreviousMonth = { viewModel.previousMonth() },
                onNextMonth = { viewModel.nextMonth() }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            CalendarGrid(
                currentMonthDate = uiState.currentMonthDate,
                selectedDate = uiState.selectedDate,
                onDateSelected = { viewModel.selectDate(it) }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Événements pour le ${uiState.selectedDate}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            EventList(events = filteredEvents)
        }
    }

    if (showAddDialog) {
        AddEventDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, desc ->
                viewModel.addEvent(title, desc, uiState.selectedDate)
                showAddDialog = false
            }
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            groupId = authState.user?.groupId ?: "Inconnu",
            userEmail = authState.user?.email ?: "",
            userName = authState.user?.displayName ?: "Utilisateur",
            userColor = authState.user?.hexColor ?: "#6200EE",
            onDismiss = { showSettingsDialog = false },
            onSignOut = {
                authViewModel.signOut()
                showSettingsDialog = false
            },
            onLeaveGroup = {
                authViewModel.joinGroup("")
                showSettingsDialog = false
            },
            onUpdateName = { newName ->
                authViewModel.updateDisplayName(newName)
            },
            onUpdateColor = { newColor ->
                authViewModel.updateColor(newColor)
            }
        )
    }
}

@Composable
fun SettingsDialog(
    groupId: String,
    userEmail: String,
    userName: String,
    userColor: String,
    onDismiss: () -> Unit,
    onSignOut: () -> Unit,
    onLeaveGroup: () -> Unit,
    onUpdateName: (String) -> Unit,
    onUpdateColor: (String) -> Unit
) {
    var isEditingName by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(userName) }

    val availableColors = listOf(
        "#F44336", "#E91E63", "#9C27B0", "#673AB7", "#3F51B5",
        "#2196F3", "#03A9F4", "#00BCD4", "#009688", "#4CAF50",
        "#8BC34A", "#CDDC39", "#FFEB3B", "#FFC107", "#FF9800", "#FF5722"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Paramètres") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Profil :", style = MaterialTheme.typography.labelLarge)
                
                if (isEditingName) {
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("Nom d'utilisateur") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { isEditingName = false }) { Text("Annuler") }
                        TextButton(onClick = {
                            onUpdateName(editedName)
                            isEditingName = false
                        }) { Text("Enregistrer") }
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(40.dp).padding(end = 12.dp),
                                shape = MaterialTheme.shapes.small,
                                color = userColor.toColor()
                            ) {}
                            Column {
                                Text(text = userName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                Text(text = userEmail, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        TextButton(onClick = { isEditingName = true }) {
                            Text("Modifier")
                        }
                    }
                }

                Text(text = "Couleur du profil :", style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableColors.take(8).forEach { colorHex ->
                        Surface(
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { onUpdateColor(colorHex) },
                            shape = MaterialTheme.shapes.extraSmall,
                            color = colorHex.toColor(),
                            border = if (colorHex == userColor) BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface) else null
                        ) {}
                    }
                }
                
                HorizontalDivider()
                
                Text(text = "Groupe actuel :", style = MaterialTheme.typography.labelLarge)
                Text(text = groupId, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                
                Button(
                    onClick = onLeaveGroup,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Changer de groupe")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onSignOut) {
                Text("Se déconnecter", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )
}

@Composable
fun AddEventDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouvel événement") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titre") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Sentences
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(title, desc) }) { Text("Ajouter") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

@Composable
fun CalendarHeader(
    currentMonthDate: LocalDate,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Mois précédent")
        }
        
        Text(
            text = "${currentMonthDate.month.name} ${currentMonthDate.year}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        IconButton(onClick = onNextMonth) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Mois suivant")
        }
    }
}

@Composable
fun CalendarGrid(
    currentMonthDate: LocalDate,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val firstOfMonth = LocalDate(currentMonthDate.year, currentMonthDate.month, 1)
    val startOffset = firstOfMonth.dayOfWeek.ordinal // Monday = 0
    
    val daysInMonth = when (firstOfMonth.month) {
        Month.JANUARY, Month.MARCH, Month.MAY, Month.JULY, Month.AUGUST, Month.OCTOBER, Month.DECEMBER -> 31
        Month.APRIL, Month.JUNE, Month.SEPTEMBER, Month.NOVEMBER -> 30
        Month.FEBRUARY -> if ((firstOfMonth.year % 4 == 0 && firstOfMonth.year % 100 != 0) || (firstOfMonth.year % 400 == 0)) 29 else 28
        else -> 31
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim").forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )
        }
    }
    
    Spacer(modifier = Modifier.height(8.dp))

    var dayCounter = 1
    for (row in 0..5) {
        Row(modifier = Modifier.fillMaxWidth()) {
            for (col in 0..6) {
                val currentDayIndex = row * 7 + col
                if (currentDayIndex < startOffset || dayCounter > daysInMonth) {
                    Box(modifier = Modifier.weight(1f))
                } else {
                    val date = LocalDate(currentMonthDate.year, currentMonthDate.month, dayCounter)
                    val isSelected = date == selectedDate
                    
                    DayCell(
                        day = dayCounter,
                        isSelected = isSelected,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onDateSelected(date) }
                    )
                    dayCounter++
                }
            }
        }
        if (dayCounter > daysInMonth) break
    }
}

@Composable
fun DayCell(
    day: Int,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = day.toString())
        }
    }
}

@Composable
fun EventList(events: List<CalendarEvent>) {
    if (events.isEmpty()) {
        Text(
            text = "Aucun événement prévu.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(vertical = 16.dp)
        )
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(events) { event ->
                EventItem(event)
            }
        }
    }
}

@Composable
fun EventItem(event: CalendarEvent) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Surface(
                modifier = Modifier.width(6.dp).fillMaxHeight(),
                color = event.authorHexColor.toColor()
            ) {}
            
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = event.title, style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = event.authorName, 
                        style = MaterialTheme.typography.labelSmall,
                        color = event.authorHexColor.toColor(),
                        fontWeight = FontWeight.Bold
                    )
                }
                if (event.description.isNotEmpty()) {
                    Text(text = event.description, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
