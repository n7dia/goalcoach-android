package com.example.goalcoach.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
import com.example.goalcoach.viewmodels.PlacesViewModel
import androidx.compose.runtime.*
import com.example.goalcoach.models.PlaceCandidate

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
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add place")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Places", style = MaterialTheme.typography.headlineSmall)


            if (vm.isLoading.value) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (saved.isEmpty()) {
                Text("No saved places yet. Tap + to add one.")
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    items(saved, key = { it.id }) { s ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFf2eef5)
                            )
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(s.name, style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(4.dp))
                                Text(s.cityState, style = MaterialTheme.typography.bodyMedium)
                                Text("${"%.5f".format(s.lat)}, ${"%.5f".format(s.lon)}", style = MaterialTheme.typography.bodySmall)
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
        title = { Text("Add a place") },
        text = {
            LazyColumn {
                items(candidates) { c ->
                    when (c) {
                        is PlaceCandidate.CurrentLatLon -> {
                            Box(Modifier.clickable { onPick(c) }) {
                                ListItem(
                                    headlineContent = { Text("Use my current location") },
                                    supportingContent = { Text("${"%.5f".format(c.lat)}, ${"%.5f".format(c.lon)}") },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }

                            Divider()
                            // Make the entire list item clickable:
                            // (Material3 ListItem doesn't have onClick param; wrap it)
                        }
                        is PlaceCandidate.Nearby -> {
                            Box(Modifier.clickable { onPick(c) }) {
                                ListItem(
                                    headlineContent = { Text(c.place.name) },
                                    supportingContent = { Text("${"%.5f".format(c.place.lat)}, ${"%.5f".format(c.place.lon)}") },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                            Divider()
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun NamePlaceDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Name") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Place name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                enabled = text.trim().isNotEmpty(),
                onClick = { onSave(text.trim()) }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
