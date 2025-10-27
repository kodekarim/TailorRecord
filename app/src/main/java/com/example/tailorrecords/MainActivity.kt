package com.example.tailorrecords

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.tailorrecords.navigation.Screen
import com.example.tailorrecords.ui.screens.*
import com.example.tailorrecords.ui.theme.TailorRecordsTheme
import com.example.tailorrecords.viewmodel.CustomerViewModel
import com.example.tailorrecords.viewmodel.OrderViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TailorRecordsTheme {
                TailorRecordsApp()
            }
        }
    }
}

@Composable
fun TailorRecordsApp() {
    val navController = rememberNavController()
    val customerViewModel: CustomerViewModel = viewModel()
    val orderViewModel: OrderViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.CustomerList.route,
        modifier = Modifier.fillMaxSize()
    ) {
        // Customer routes
        composable(Screen.CustomerList.route) {
            CustomerListScreen(navController, customerViewModel)
        }

        composable(Screen.AddCustomer.route) {
            AddEditCustomerScreen(navController, viewModel = customerViewModel)
        }

        composable(
            route = Screen.EditCustomer.route,
            arguments = listOf(navArgument("customerId") { type = NavType.LongType })
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getLong("customerId") ?: 0L
            AddEditCustomerScreen(navController, customerId, customerViewModel)
        }

        composable(
            route = Screen.CustomerDetail.route,
            arguments = listOf(navArgument("customerId") { type = NavType.LongType })
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getLong("customerId") ?: 0L
            CustomerDetailScreen(navController, customerId, customerViewModel, orderViewModel)
        }

        // Measurement routes
        composable(
            route = Screen.AddMeasurement.route,
            arguments = listOf(navArgument("customerId") { type = NavType.LongType })
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getLong("customerId") ?: 0L
            AddEditMeasurementScreen(navController, customerId, viewModel = customerViewModel)
        }

        composable(
            route = Screen.EditMeasurement.route,
            arguments = listOf(
                navArgument("customerId") { type = NavType.LongType },
                navArgument("measurementId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getLong("customerId") ?: 0L
            val measurementId = backStackEntry.arguments?.getLong("measurementId") ?: 0L
            AddEditMeasurementScreen(navController, customerId, measurementId, customerViewModel)
        }

        // Order routes
        composable(Screen.OrderList.route) {
            OrderListScreen(navController, orderViewModel)
        }

        composable(
            route = Screen.AddOrder.route,
            arguments = listOf(navArgument("customerId") { type = NavType.LongType })
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getLong("customerId") ?: 0L
            AddEditOrderScreen(navController, customerId, orderViewModel = orderViewModel, customerViewModel = customerViewModel)
        }

        composable(
            route = Screen.EditOrder.route,
            arguments = listOf(navArgument("orderId") { type = NavType.LongType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getLong("orderId") ?: 0L
            AddEditOrderScreen(
                navController = navController,
                customerId = null, // customerId will be fetched from the order
                orderId = orderId,
                orderViewModel = orderViewModel,
                customerViewModel = customerViewModel
            )
        }

        // Settings route
        composable(Screen.Settings.route) {
            SettingsScreen(navController)
        }
    }
}