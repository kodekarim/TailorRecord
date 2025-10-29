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
            arguments = listOf(
                navArgument("customerId") { type = NavType.LongType },
                navArgument("tab") { 
                    type = NavType.IntType
                    defaultValue = 0
                }
            )
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getLong("customerId") ?: 0L
            val initialTab = backStackEntry.arguments?.getInt("tab") 
                ?: navController.previousBackStackEntry?.savedStateHandle?.get<Int>("selectedTab") 
                ?: 0
            CustomerDetailScreen(navController, customerId, initialTab, customerViewModel, orderViewModel)
        }

        // Measurement routes
        composable(
            route = Screen.AddMeasurement.route,
            arguments = listOf(navArgument("customerId") { type = NavType.LongType })
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getLong("customerId") ?: 0L
            AddEditMeasurementScreen(navController, customerId, customerViewModel = customerViewModel)
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
            arguments = listOf(
                navArgument("orderId") { type = NavType.LongType },
                navArgument("customerId") { 
                    type = NavType.LongType
                    defaultValue = 0L
                },
                navArgument("returnTab") { 
                    type = NavType.IntType
                    defaultValue = 0
                }
            )
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getLong("orderId") ?: 0L
            val customerId = backStackEntry.arguments?.getLong("customerId")?.takeIf { it > 0 }
            val returnTab = backStackEntry.arguments?.getInt("returnTab") ?: 0
            AddEditOrderScreen(
                navController = navController,
                customerId = customerId,
                orderId = orderId,
                orderViewModel = orderViewModel,
                customerViewModel = customerViewModel,
                returnToCustomerTab = returnTab
            )
        }

        composable(
            route = Screen.OrderCompletion.route,
            arguments = listOf(
                navArgument("orderId") { type = NavType.LongType },
                navArgument("customerName") { type = NavType.StringType },
                navArgument("customerPhone") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getLong("orderId") ?: 0L
            val customerName = backStackEntry.arguments?.getString("customerName") ?: ""
            val customerPhone = backStackEntry.arguments?.getString("customerPhone") ?: ""
            OrderCompletionScreen(
                navController = navController,
                orderId = orderId,
                customerName = customerName,
                customerPhone = customerPhone
            )
        }

        // Settings route
        composable(Screen.Settings.route) {
            SettingsScreen(navController)
        }
    }
}