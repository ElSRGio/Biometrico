package com.example.biometricos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.biometricos.ui.dashboard.DashboardScreen
import com.example.biometricos.ui.dashboard.DashboardViewModel
import com.example.biometricos.ui.focus.FocusModeScreen
import com.example.biometricos.ui.focus.FocusViewModel
import com.example.biometricos.ui.theme.BiometricosTheme
import kotlinx.serialization.Serializable

@Serializable object DashboardRoute
@Serializable object FocusModeRoute

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BiometricosTheme {
                MainNavigation()
            }
        }
    }
}

@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = "dashboard") {
        composable("dashboard") {
            val viewModel: DashboardViewModel = viewModel()
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToFocus = { navController.navigate("focus") }
            )
        }
        composable("focus") {
            val viewModel: FocusViewModel = viewModel()
            FocusModeScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
