package com.josuefp.servicenotebook.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.josuefp.servicenotebook.ui.theme.ServiceNotebookTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    appName: String = "Virna Nails",
    onUpdateAppName: (String) -> Unit = {},
    onNavigateToInventory: () -> Unit,
    onNavigateToCatalog: () -> Unit,
    onNavigateToAgenda: () -> Unit,
    onNavigateToBackup: () -> Unit
) {
    var showRenameDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(appName) },
                actions = {
                    IconButton(onClick = { showRenameDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Personalizar Nombre")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Bienvenida a $appName",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onNavigateToInventory,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Inventario de Productos", style = MaterialTheme.typography.titleMedium)
            }

            Button(
                onClick = onNavigateToCatalog,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Catálogo y Calculadora", style = MaterialTheme.typography.titleMedium)
            }

            Button(
                onClick = onNavigateToAgenda,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Agenda y Citas", style = MaterialTheme.typography.titleMedium)
            }

            Button(
                onClick = onNavigateToBackup,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Copias de Seguridad", style = MaterialTheme.typography.titleMedium)
            }
        }
    }

    if (showRenameDialog) {
        RenameAppDialog(
            currentName = appName,
            onDismiss = { showRenameDialog = false },
            onConfirm = { newName ->
                onUpdateAppName(newName)
                showRenameDialog = false
            }
        )
    }
}

@Composable
fun RenameAppDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Personalizar Nombre de la App") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (errorMessage != null) {
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre de la aplicación") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "El nombre no puede estar vacío."
                    } else {
                        onConfirm(name.trim())
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    ServiceNotebookTheme {
        HomeScreen(
            appName = "Virna Nails",
            onUpdateAppName = {},
            onNavigateToInventory = {},
            onNavigateToCatalog = {},
            onNavigateToAgenda = {},
            onNavigateToBackup = {}
        )
    }
}
