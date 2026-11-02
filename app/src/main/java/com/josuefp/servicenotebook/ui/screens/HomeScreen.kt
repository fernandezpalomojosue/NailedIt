package com.josuefp.servicenotebook.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.josuefp.servicenotebook.ui.theme.ServiceNotebookTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    appName: String = "NailedIt",
    themePalette: String = "ROSE",
    onUpdateAppName: (String) -> Unit = {},
    onUpdateThemePalette: (String) -> Unit = {},
    onNavigateToInventory: () -> Unit,
    onNavigateToCatalog: () -> Unit,
    onNavigateToAgenda: () -> Unit,
    onNavigateToGallery: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToBackup: () -> Unit
) {
    var showSettingsDialog by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val topHeaderPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 64.dp

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(top = topHeaderPadding)
                    .fillMaxWidth(0.55f),
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = appName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Estudio & Citas 💅",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                NavigationDrawerItem(
                    label = { Text("Inventario 📦", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToInventory()
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )

                NavigationDrawerItem(
                    label = { Text("Catálogo 💅", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToCatalog()
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )

                NavigationDrawerItem(
                    label = { Text("Agenda 📅", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToAgenda()
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )

                NavigationDrawerItem(
                    label = { Text("Galería 📷", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToGallery()
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )

                NavigationDrawerItem(
                    label = { Text("Reportes 📊", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToReports()
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
                Spacer(modifier = Modifier.height(4.dp))

                NavigationDrawerItem(
                    label = { Text("Respaldos 💾", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToBackup()
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )

                NavigationDrawerItem(
                    label = { Text("Ajustes ⚙️", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        showSettingsDialog = true
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(appName) },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                }
                            }
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú de navegación")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showSettingsDialog = true }) {
                            Icon(Icons.Default.Settings, contentDescription = "Ajustes de la aplicación")
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

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onNavigateToInventory,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text("Inventario de Productos", style = MaterialTheme.typography.titleMedium)
                }

                Button(
                    onClick = onNavigateToCatalog,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text("Catálogo y Calculadora", style = MaterialTheme.typography.titleMedium)
                }

                Button(
                    onClick = onNavigateToAgenda,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text("Agenda y Citas", style = MaterialTheme.typography.titleMedium)
                }

                Button(
                    onClick = onNavigateToGallery,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text("Galería de Trabajos 📷", style = MaterialTheme.typography.titleMedium)
                }

                Button(
                    onClick = onNavigateToReports,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text("Reportes Financieros", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }

    if (showSettingsDialog) {
        AppSettingsDialog(
            currentName = appName,
            currentTheme = themePalette,
            onDismiss = { showSettingsDialog = false },
            onConfirm = { newName, newTheme ->
                onUpdateAppName(newName)
                onUpdateThemePalette(newTheme)
                showSettingsDialog = false
            },
            onNavigateToBackup = {
                showSettingsDialog = false
                onNavigateToBackup()
            }
        )
    }
}

@Composable
fun AppSettingsDialog(
    currentName: String,
    currentTheme: String,
    onDismiss: () -> Unit,
    onConfirm: (newName: String, newTheme: String) -> Unit,
    onNavigateToBackup: () -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var selectedTheme by remember { mutableStateOf(currentTheme) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val themeOptions = listOf(
        "ROSE" to ("Rosa / Magenta 🌸" to Color(0xFFD81B60)),
        "PURPLE" to ("Púrpura / Lavanda 🪻" to Color(0xFF8E24AA)),
        "TEAL" to ("Turquesa / Menta 🌿" to Color(0xFF00897B)),
        "CORAL" to ("Coral Cálido 🍑" to Color(0xFFE64A19)),
        "BLUE" to ("Azul Océano 💙" to Color(0xFF1E88E5)),
        "ELEGANT_DARK" to ("Elegante Oscuro 🖤" to Color(0xFF37474F))
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajustes de la Aplicación ⚙️") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (errorMessage != null) {
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Text("Nombre de la aplicación", style = MaterialTheme.typography.labelMedium)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(2.dp))
                Text("Tema de color principal", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    themeOptions.forEach { (key, info) ->
                        val (label, color) = info
                        val isSelected = selectedTheme.equals(key, ignoreCase = true)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedTheme = key },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) color.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .background(color, shape = CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = color)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(2.dp))

                Text("Sistema y Respaldo", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                OutlinedButton(
                    onClick = onNavigateToBackup,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Copias de Seguridad (Backup)")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "El nombre no puede estar vacío."
                    } else {
                        onConfirm(name.trim(), selectedTheme)
                    }
                }
            ) {
                Text("Guardar Cambios")
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
            appName = "NailedIt",
            themePalette = "ROSE",
            onUpdateAppName = {},
            onUpdateThemePalette = {},
            onNavigateToInventory = {},
            onNavigateToCatalog = {},
            onNavigateToAgenda = {},
            onNavigateToGallery = {},
            onNavigateToReports = {},
            onNavigateToBackup = {}
        )
    }
}
