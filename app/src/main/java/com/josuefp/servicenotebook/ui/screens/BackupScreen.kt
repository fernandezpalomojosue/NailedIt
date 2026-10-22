package com.josuefp.servicenotebook.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.josuefp.servicenotebook.data.backup.BackupManager
import com.josuefp.servicenotebook.data.local.AppDatabase
import com.josuefp.servicenotebook.ui.theme.ServiceNotebookTheme
import kotlinx.coroutines.launch

@Composable
fun BackupScreen(
    appName: String = "Virna Nails",
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = AppDatabase.getDatabase(context)
    val backupManager = BackupManager(database)

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { destinationUri ->
            scope.launch {
                try {
                    val json = backupManager.createBackupJson()
                    context.contentResolver.openOutputStream(destinationUri)?.use { output ->
                        output.write(json.toByteArray())
                    }
                    Toast.makeText(context, "Copia de seguridad exportada con éxito", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Error al exportar: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { sourceUri ->
            scope.launch {
                try {
                    val json = context.contentResolver.openInputStream(sourceUri)?.bufferedReader()?.use { it.readText() }
                    if (json != null) {
                        val success = backupManager.restoreFromBackupJson(json)
                        if (success) {
                            Toast.makeText(context, "Datos restaurados correctamente", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "El archivo de copia no es válido", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error al restaurar: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    BackupScreenContent(
        appName = appName,
        onExportClick = { exportLauncher.launch("virna_nails_backup.json") },
        onImportClick = { importLauncher.launch(arrayOf("application/json", "*/*")) },
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreenContent(
    appName: String,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$appName - Copias de Seguridad") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Exportar Copia", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Guarda todos los productos, catálogo, clientes y citas en un archivo JSON en tu dispositivo.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onExportClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Exportar Datos a Archivo JSON")
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Restaurar Copia", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Restaura la información desde un archivo JSON previamente exportado.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onImportClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Restaurar Datos desde Archivo JSON")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BackupScreenPreview() {
    ServiceNotebookTheme {
        BackupScreenContent(
            appName = "Virna Nails",
            onExportClick = {},
            onImportClick = {},
            onNavigateBack = {}
        )
    }
}
