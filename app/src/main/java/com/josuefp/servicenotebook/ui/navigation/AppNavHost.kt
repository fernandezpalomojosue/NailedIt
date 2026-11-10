package com.josuefp.servicenotebook.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.josuefp.servicenotebook.data.local.AppDatabase
import com.josuefp.servicenotebook.data.repository.AgendaRepository
import com.josuefp.servicenotebook.data.repository.InventoryRepository
import com.josuefp.servicenotebook.ui.screens.AgendaScreen
import com.josuefp.servicenotebook.ui.screens.AppointmentFormScreen
import com.josuefp.servicenotebook.ui.screens.AppSettingsDialog
import com.josuefp.servicenotebook.ui.screens.BackupScreen
import com.josuefp.servicenotebook.ui.screens.CatalogScreen
import com.josuefp.servicenotebook.ui.screens.GalleryScreen
import com.josuefp.servicenotebook.ui.screens.HomeScreen
import com.josuefp.servicenotebook.ui.screens.InventoryScreen
import com.josuefp.servicenotebook.ui.screens.ReportsScreen
import com.josuefp.servicenotebook.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost(
    settingsViewModel: SettingsViewModel,
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val agendaRepository = remember { AgendaRepository(database) }
    val inventoryRepository = remember { InventoryRepository(database) }
    val scope = rememberCoroutineScope()

    val appName by settingsViewModel.appName.collectAsState()
    val themePalette by settingsViewModel.themePalette.collectAsState()

    val allAppointments by agendaRepository.getAllAppointmentsWithDetails().collectAsState(initial = emptyList())
    val allProducts by inventoryRepository.getAllProducts().collectAsState(initial = emptyList())
    val allClients by agendaRepository.getAllClients().collectAsState(initial = emptyList())

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var showSettingsDialog by remember { mutableStateOf(false) }

    val topHeaderPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 64.dp

    val onOpenDrawer = {
        scope.launch {
            if (drawerState.isClosed) drawerState.open() else drawerState.close()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
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
                            text = "Estudio & Citas",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
                    label = { Text("Inventario", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(NavRoute.Inventory.route) {
                            popUpTo(NavRoute.Home.route) { saveState = true }
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Star, contentDescription = null) },
                    label = { Text("Catálogo", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(NavRoute.Catalog.route) {
                            popUpTo(NavRoute.Home.route) { saveState = true }
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    label = { Text("Agenda", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(NavRoute.Agenda.route) {
                            popUpTo(NavRoute.Home.route) { saveState = true }
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                    label = { Text("Galería", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(NavRoute.Gallery.route) {
                            popUpTo(NavRoute.Home.route) { saveState = true }
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                    label = { Text("Reportes", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(NavRoute.Reports.route) {
                            popUpTo(NavRoute.Home.route) { saveState = true }
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
                Spacer(modifier = Modifier.height(4.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Respaldos", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(NavRoute.Backup.route) {
                            popUpTo(NavRoute.Home.route) { saveState = true }
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Ajustes", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold) },
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
        NavHost(
            navController = navController,
            startDestination = NavRoute.Home.route
        ) {
            composable(NavRoute.Home.route) {
                HomeScreen(
                    appName = appName,
                    themePalette = themePalette,
                    allAppointments = allAppointments,
                    allProducts = allProducts,
                    allClients = allClients,
                    onOpenDrawer = { onOpenDrawer() },
                    onOpenSettings = { showSettingsDialog = true },
                    onUpdateAppointmentStatus = { appId, newStatus ->
                        scope.launch { agendaRepository.updateAppointmentStatus(appId, newStatus) }
                    },
                    onNavigateToInventory = { navController.navigate(NavRoute.Inventory.route) },
                    onNavigateToCatalog = { navController.navigate(NavRoute.Catalog.route) },
                    onNavigateToAgenda = { navController.navigate(NavRoute.Agenda.route) },
                    onNavigateToAppointmentForm = { appointmentId, dateMillis ->
                        navController.navigate(NavRoute.AppointmentForm.createRoute(appointmentId, dateMillis))
                    }
                )
            }
            composable(NavRoute.Inventory.route) {
                InventoryScreen(
                    appName = appName,
                    onOpenDrawer = { onOpenDrawer() },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(NavRoute.Catalog.route) {
                CatalogScreen(
                    appName = appName,
                    onOpenDrawer = { onOpenDrawer() },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(NavRoute.Agenda.route) {
                AgendaScreen(
                    appName = appName,
                    onOpenDrawer = { onOpenDrawer() },
                    onNavigateToAppointmentForm = { appointmentId, dateMillis ->
                        navController.navigate(NavRoute.AppointmentForm.createRoute(appointmentId, dateMillis))
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(NavRoute.Gallery.route) {
                GalleryScreen(
                    appName = appName,
                    onOpenDrawer = { onOpenDrawer() },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(NavRoute.Backup.route) {
                BackupScreen(
                    appName = appName,
                    onOpenDrawer = { onOpenDrawer() },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(NavRoute.Reports.route) {
                ReportsScreen(
                    appName = appName,
                    onOpenDrawer = { onOpenDrawer() },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "appointment_form?appointmentId={appointmentId}&dateMillis={dateMillis}",
                arguments = listOf(
                    navArgument("appointmentId") { type = NavType.LongType; defaultValue = 0L },
                    navArgument("dateMillis") { type = NavType.LongType; defaultValue = System.currentTimeMillis() }
                )
            ) { backStackEntry ->
                val appointmentId = backStackEntry.arguments?.getLong("appointmentId") ?: 0L
                val dateMillis = backStackEntry.arguments?.getLong("dateMillis") ?: System.currentTimeMillis()
                AppointmentFormScreen(
                    appName = appName,
                    appointmentId = appointmentId,
                    initialDateMillis = dateMillis,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }

    if (showSettingsDialog) {
        AppSettingsDialog(
            currentName = appName,
            currentTheme = themePalette,
            onDismiss = { showSettingsDialog = false },
            onConfirm = { newName, newTheme ->
                settingsViewModel.updateAppName(newName)
                settingsViewModel.updateThemePalette(newTheme)
                showSettingsDialog = false
            },
            onNavigateToBackup = {
                showSettingsDialog = false
                navController.navigate(NavRoute.Backup.route)
            }
        )
    }
}
