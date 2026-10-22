package com.josuefp.servicenotebook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.josuefp.servicenotebook.ui.navigation.AppNavHost
import com.josuefp.servicenotebook.ui.theme.ServiceNotebookTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ServiceNotebookTheme {
                AppNavHost()
            }
        }
    }
}
