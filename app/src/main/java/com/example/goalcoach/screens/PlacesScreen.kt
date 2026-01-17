package com.example.goalcoach.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.goalcoach.models.PlaceCandidate
import com.example.goalcoach.viewmodels.PlacesViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacesScreen(vm: PlacesViewModel) {

    val saved by vm.saved.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar whenever error changes
    LaunchedEffect(vm.error.value) {
        val msg = vm.error.value ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message = "Error: $msg")
        vm.error.value = null // clear so it doesn't repeat
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val fine = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarse = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fine || coarse) vm.onAddClicked()
        else vm.error.value = "Location permission denied."
    }

    Scaffold(
        containerColor = Color.White,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add place")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                "Places that inspire and motivate you.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF8E8E93),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (vm.isLoading.value) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF757575)
                )
            }

            if (saved.isEmpty()) {
                Text(
                    "No saved places yet. Tap + to add one.",
                    color = Color(0xFF8E8E93)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    items(saved, key = { it.id }) { s ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            )
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    s.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1C1C1E)
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    s.cityState,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF1C1C1E)
                                )
                                Text(
                                    "${"%.5f".format(s.lat)}, ${"%.5f".format(s.lon)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF8E8E93)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (vm.showPicker.value) {
        PlacePickerDialog(
            candidates = vm.candidates.value,
            onDismiss = { vm.showPicker.value = false },
            onPick = vm::onCandidateSelected
        )
    }

    if (vm.showNameDialog.value) {
        NamePlaceDialog(
            onDismiss = { vm.showNameDialog.value = false },
            onSave = { name -> vm.saveNamedCurrentLocation(name) }
        )
    }
}

@Composable
private fun PlacePickerDialog(
    candidates: List<PlaceCandidate>,
    onDismiss: () -> Unit,
    onPick: (PlaceCandidate) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                "Add a place",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
            )
        },
        text = {
            LazyColumn {
                items(candidates) { c ->
                    when (c) {
                        is PlaceCandidate.CurrentLatLon -> {
                            Box(Modifier.clickable { onPick(c) }) {
                                ListItem(
                                    headlineContent = { 
                                        Text(
                                            "Use my current location",
                                            fontWeight = FontWeight.Medium
                                        )
                                    },
                                    supportingContent = { 
                                        Text(
                                            "${"%.5f".format(c.lat)}, ${"%.5f".format(c.lon)}",
                                            color = Color(0xFF8E8E93)
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }

                            Divider(color = Color(0xFFE5E5EA))
                        }
                        is PlaceCandidate.Nearby -> {
                            Box(Modifier.clickable { onPick(c) }) {
                                ListItem(
                                    headlineContent = { 
                                        Text(
                                            c.place.name,
                                            fontWeight = FontWeight.Medium
                                        )
                                    },
                                    supportingContent = { 
                                        Text(
                                            "${"%.5f".format(c.place.lat)}, ${"%.5f".format(c.place.lon)}",
                                            color = Color(0xFF8E8E93)
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                            Divider(color = Color(0xFFE5E5EA))
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { 
                Text("Close", color = Color(0xFF757575))
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun NamePlaceDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val onPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                "Name",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
            )
        },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Place name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF757575),
                    unfocusedBorderColor = Color(0xFFE5E5EA)
                )
            )
        },
        confirmButton = {
            TextButton(
                enabled = text.trim().isNotEmpty(),
                onClick = { onSave(text.trim()) }
            ) { 
                Text(
                    "Save",
                    color = if (text.trim().isNotEmpty()) onPrimaryContainer else Color(0xFF8E8E93),
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { 
                Text("Cancel", color = Color(0xFF757575))
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
