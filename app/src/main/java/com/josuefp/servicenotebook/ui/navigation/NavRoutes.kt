package com.josuefp.servicenotebook.ui.navigation

sealed class NavRoute(val route: String) {
    data object Home : NavRoute("home")
    data object Inventory : NavRoute("inventory")
    data object Catalog : NavRoute("catalog")
    data object Agenda : NavRoute("agenda")
    data object Backup : NavRoute("backup")
}
