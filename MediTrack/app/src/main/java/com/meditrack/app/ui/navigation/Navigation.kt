package com.meditrack.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.meditrack.app.ui.screens.AddMedicineScreen
import com.meditrack.app.ui.screens.ContactsScreen
import com.meditrack.app.ui.screens.HistoryScreen
import com.meditrack.app.ui.screens.MedicinesScreen
import com.meditrack.app.ui.screens.TodayScreen
import com.meditrack.app.ui.theme.Background
import com.meditrack.app.ui.theme.Muted
import com.meditrack.app.ui.theme.Primary
import com.meditrack.app.ui.theme.PrimaryLight

sealed class Destination(val route: String, val label: String, val icon: ImageVector) {
    data object Today : Destination("today", "Today", Icons.Filled.Home)
    data object Medicines : Destination("medicines", "Medicines", Icons.Filled.MedicalServices)
    data object History : Destination("history", "History", Icons.Filled.History)
    data object Contacts : Destination("contacts", "Contacts", Icons.Filled.Person)
}

private val bottomItems = listOf(
    Destination.Today, Destination.Medicines, Destination.History, Destination.Contacts
)

private const val ADD_ROUTE = "add_medicine"
private const val ADD_PATTERN = "$ADD_ROUTE?medicineId={medicineId}"

@Composable
fun MediTrackNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = bottomItems.any { it.route == currentRoute }

    fun navigateTop(dest: Destination) {
        navController.navigate(dest.route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        containerColor = Background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = Color.White) {
                    bottomItems.forEach { dest ->
                        NavigationBarItem(
                            selected = currentRoute == dest.route,
                            onClick = { navigateTop(dest) },
                            icon = { Icon(dest.icon, contentDescription = dest.label) },
                            label = { Text(dest.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Primary,
                                selectedTextColor = Primary,
                                indicatorColor = PrimaryLight,
                                unselectedIconColor = Muted,
                                unselectedTextColor = Muted
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Today.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Destination.Today.route) {
                TodayScreen(onSeeAll = { navigateTop(Destination.History) })
            }
            composable(Destination.Medicines.route) {
                MedicinesScreen(
                    onAdd = { navController.navigate(ADD_ROUTE) },
                    onEdit = { id -> navController.navigate("$ADD_ROUTE?medicineId=$id") }
                )
            }
            composable(Destination.History.route) { HistoryScreen() }
            composable(Destination.Contacts.route) { ContactsScreen() }
            composable(
                route = ADD_PATTERN,
                arguments = listOf(navArgument("medicineId") {
                    type = NavType.LongType
                    defaultValue = 0L
                })
            ) { entry ->
                AddMedicineScreen(
                    medicineId = entry.arguments?.getLong("medicineId") ?: 0L,
                    onClose = { navController.popBackStack() }
                )
            }
        }
    }
}
