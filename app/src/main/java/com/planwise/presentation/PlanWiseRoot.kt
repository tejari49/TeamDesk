package com.planwise.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.planwise.R
import com.planwise.presentation.screens.*
import com.planwise.presentation.viewmodel.MainViewModel

sealed class BottomTab(val route: String, val labelRes: Int, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    data object Overview : BottomTab("overview", R.string.tab_overview, androidx.compose.material.icons.Icons.Outlined.Home)
    data object Events : BottomTab("events", R.string.tab_events, androidx.compose.material.icons.Icons.Outlined.Event)
    data object Shifts : BottomTab("shifts", R.string.tab_shifts, androidx.compose.material.icons.Icons.Outlined.CalendarMonth)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanWiseRoot() {
    val navController = rememberNavController()
    val mainVm: MainViewModel = hiltViewModel()
    val uiState by mainVm.uiState.collectAsState()

    val tabs = listOf(BottomTab.Overview, BottomTab.Events, BottomTab.Shifts)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Outlined.Settings,
                            contentDescription = stringResource(R.string.settings)
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                val currentBackStack by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStack?.destination?.route
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = stringResource(tab.labelRes)) },
                        label = { Text(stringResource(tab.labelRes)) }
                    )
                }
            }
        }
    ) { inner ->
        NavHost(
            navController = navController,
            startDestination = BottomTab.Overview.route,
            modifier = Modifier.padding(inner)
        ) {
            composable(BottomTab.Overview.route) {
                OverviewScreen(
                    onQuickAdd = { navController.navigate("event_edit?eventId=") },
                    onOpenEvent = { id -> navController.navigate("event_detail/$id") }
                )
            }
            composable(BottomTab.Events.route) {
                EventsScreen(
                    onCreate = { navController.navigate("event_edit?eventId=") },
                    onOpenEvent = { id -> navController.navigate("event_detail/$id") }
                )
            }
            composable(BottomTab.Shifts.route) { ShiftsScreen() }

            composable("settings") { SettingsScreen(onBack = { navController.popBackStack() }) }

            composable(
                route = "event_detail/{id}",
                arguments = listOf(navArgument("id") { type = androidx.navigation.NavType.StringType })
            ) { backStack ->
                val id = backStack.arguments?.getString("id") ?: return@composable
                EventDetailScreen(
                    eventId = id,
                    onEdit = { navController.navigate("event_edit?eventId=$id") },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "event_edit?eventId={eventId}",
                arguments = listOf(navArgument("eventId") { type = androidx.navigation.NavType.StringType; defaultValue = "" })
            ) { backStack ->
                val eventId = backStack.arguments?.getString("eventId").orEmpty().ifBlank { null }
                EventEditScreen(
                    eventId = eventId,
                    onDone = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() }
                )
            }
        }
    }
}
