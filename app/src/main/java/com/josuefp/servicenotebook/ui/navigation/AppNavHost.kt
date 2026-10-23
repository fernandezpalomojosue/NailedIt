package com.josuefp.servicenotebook.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.josuefp.servicenotebook.data.local.AppDatabase
import com.josuefp.servicenotebook.data.repository.SettingsRepository
import com.josuefp.servicenotebook.ui.screens.AgendaScreen
import com.josuefp.servicenotebook.ui.screens.AppointmentFormScreen
import com.josuefp.servicenotebook.ui.screens.BackupScreen
import com.josuefp.servicenotebook.ui.screens.CatalogScreen
import com.josuefp.servicenotebook.ui.screens.GalleryScreen
import com.josuefp.servicenotebook.ui.screens.HomeScreen
import com.josuefp.servicenotebook.ui.screens.InventoryScreen
import com.josuefp.servicenotebook.ui.screens.ReportsScreen
import com.josuefp.servicenotebook.ui.viewmodel.SettingsViewModel

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val settingsRepository = remember { SettingsRepository(database) }
    val settingsViewModel = remember { SettingsViewModel(settingsRepository) }
    val appName by settingsViewModel.appName.collectAsState()

    NavHost(
        navController = navController,
        startDestination = NavRoute.Home.route
    ) {
        composable(NavRoute.Home.route) {
            HomeScreen(
                appName = appName,
                onUpdateAppName = settingsViewModel::updateAppName,
                onNavigateToInventory = { navController.navigate(NavRoute.Inventory.route) },
                onNavigateToCatalog = { navController.navigate(NavRoute.Catalog.route) },
                onNavigateToAgenda = { navController.navigate(NavRoute.Agenda.route) },
                onNavigateToGallery = { navController.navigate(NavRoute.Gallery.route) },
                onNavigateToReports = { navController.navigate(NavRoute.Reports.route) },
                onNavigateToBackup = { navController.navigate(NavRoute.Backup.route) }
            )
        }
        composable(NavRoute.Inventory.route) {
            InventoryScreen(
                appName = appName,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(NavRoute.Catalog.route) {
            CatalogScreen(
                appName = appName,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(NavRoute.Agenda.route) {
            AgendaScreen(
                appName = appName,
                onNavigateToAppointmentForm = { appointmentId, dateMillis ->
                    navController.navigate(NavRoute.AppointmentForm.createRoute(appointmentId, dateMillis))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(NavRoute.Gallery.route) {
            GalleryScreen(
                appName = appName,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(NavRoute.Backup.route) {
            BackupScreen(
                appName = appName,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(NavRoute.Reports.route) {
            ReportsScreen(
                appName = appName,
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
