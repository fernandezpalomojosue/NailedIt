package com.josuefp.servicenotebook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.josuefp.servicenotebook.data.local.AppDatabase
import com.josuefp.servicenotebook.data.repository.SettingsRepository
import com.josuefp.servicenotebook.ui.navigation.AppNavHost
import com.josuefp.servicenotebook.ui.theme.ServiceNotebookTheme
import com.josuefp.servicenotebook.ui.viewmodel.SettingsViewModel
import com.josuefp.servicenotebook.ui.viewmodel.SettingsViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val database = remember { AppDatabase.getDatabase(context) }
            val repository = remember { SettingsRepository(database) }
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(repository)
            )
            val themePalette by settingsViewModel.themePalette.collectAsState()

            ServiceNotebookTheme(themePalette = themePalette) {
                AppNavHost(settingsViewModel = settingsViewModel)
            }
        }
    }
}
