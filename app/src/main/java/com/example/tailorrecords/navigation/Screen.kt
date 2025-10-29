package com.example.tailorrecords.navigation

sealed class Screen(val route: String) {
    object CustomerList : Screen("customer_list")
    object AddCustomer : Screen("add_customer")
    object EditCustomer : Screen("edit_customer/{customerId}") {
        fun createRoute(customerId: Long) = "edit_customer/$customerId"
    }
    object CustomerDetail : Screen("customer_detail/{customerId}?tab={tab}") {
        fun createRoute(customerId: Long, selectedTab: Int = 0) = "customer_detail/$customerId?tab=$selectedTab"
    }
    object AddMeasurement : Screen("add_measurement/{customerId}") {
        fun createRoute(customerId: Long) = "add_measurement/$customerId"
    }
    object EditMeasurement : Screen("edit_measurement/{customerId}/{measurementId}") {
        fun createRoute(customerId: Long, measurementId: Long) = "edit_measurement/$customerId/$measurementId"
    }
    object OrderList : Screen("order_list")
    object AddOrder : Screen("add_order/{customerId}") {
        fun createRoute(customerId: Long) = "add_order/$customerId"
    }
    object EditOrder : Screen("edit_order/{orderId}") {
        fun createRoute(orderId: Long) = "edit_order/$orderId"
    }
    object OrderDetail : Screen("order_detail/{orderId}") {
        fun createRoute(orderId: Long) = "order_detail/$orderId"
    }
    object OrderCompletion : Screen("order_completion/{orderId}/{customerName}/{customerPhone}") {
        fun createRoute(orderId: Long, customerName: String, customerPhone: String) =
            "order_completion/$orderId/$customerName/$customerPhone"
    }
    object Settings : Screen("settings")
}

