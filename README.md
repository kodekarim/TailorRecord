# Tailor Records

A lightweight Android app for tailors to record, manage, and retrieve customer measurements and orders.

## Features

### Customer Management
- ✅ Add, edit, and delete customers
- ✅ Store customer contact information (name, phone, address)
- ✅ Search customers by name or phone number
- ✅ Add notes for each customer

### Measurement Management
- ✅ Record detailed body measurements for each customer
- ✅ Support for upper body measurements (shirt length, shoulder, chest, waist, sleeve, neck, armhole)
- ✅ Support for lower body measurements (trouser length, hip, thigh, knee, bottom, inseam)
- ✅ View measurement history for each customer
- ✅ Add notes for each measurement record

### Order Management
- ✅ Create and manage orders for customers
- ✅ Track order details (item type, quantity, description)
- ✅ Manage pricing (total price, advance payment, remaining balance)
- ✅ Order status tracking (Pending, In Progress, Completed, Delivered, Cancelled)
- ✅ Set due dates for orders
- ✅ Filter orders by status
- ✅ View overdue orders highlighted in red

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: Room (SQLite)
- **Navigation**: Jetpack Navigation Compose
- **Minimum SDK**: 31 (Android 12)
- **Target SDK**: 36

## Project Structure

```
app/src/main/java/com/example/tailorrecords/
├── data/
│   ├── models/
│   │   ├── Customer.kt          # Customer data model
│   │   ├── Measurement.kt       # Measurement data model
│   │   └── Order.kt             # Order data model
│   ├── dao/
│   │   ├── CustomerDao.kt       # Customer database operations
│   │   ├── MeasurementDao.kt    # Measurement database operations
│   │   └── OrderDao.kt          # Order database operations
│   ├── repository/
│   │   └── TailorRepository.kt  # Repository layer
│   ├── Converters.kt            # Room type converters
│   └── TailorDatabase.kt        # Room database setup
├── viewmodel/
│   ├── CustomerViewModel.kt     # Customer business logic
│   └── OrderViewModel.kt        # Order business logic
├── ui/
│   ├── screens/
│   │   ├── CustomerListScreen.kt
│   │   ├── AddEditCustomerScreen.kt
│   │   ├── CustomerDetailScreen.kt
│   │   ├── AddEditMeasurementScreen.kt
│   │   ├── OrderListScreen.kt
│   │   └── AddEditOrderScreen.kt
│   └── theme/                   # App theming
├── navigation/
│   └── Screen.kt                # Navigation routes
└── MainActivity.kt              # Main entry point
```

## Database Schema

### Customer Table
- `id` (Primary Key)
- `name`
- `phoneNumber`
- `address`
- `notes`
- `createdAt`

### Measurement Table
- `id` (Primary Key)
- `customerId` (Foreign Key)
- Upper body: `shirtLength`, `shoulder`, `chest`, `waist`, `sleeve`, `neck`, `armhole`
- Lower body: `trouserLength`, `hip`, `thigh`, `knee`, `bottom`, `inseam`
- `notes`
- `createdAt`
- `updatedAt`

### Order Table
- `id` (Primary Key)
- `customerId` (Foreign Key)
- `itemType`
- `quantity`
- `description`
- `price`
- `advancePaid`
- `status` (Enum: PENDING, IN_PROGRESS, COMPLETED, DELIVERED, CANCELLED)
- `orderDate`
- `dueDate`
- `completedDate`
- `notes`

## Getting Started

### Prerequisites
- Android Studio Hedgehog or newer
- JDK 11 or newer
- Android SDK 31 or higher

### Installation

1. Clone the repository:
```bash
git clone https://github.com/yourusername/TailorRecord.git
cd TailorRecord
```

2. Open the project in Android Studio

3. Sync Gradle files

4. Run the app on an emulator or physical device

## Usage

### Adding a Customer
1. Open the app
2. Tap the '+' floating action button
3. Fill in customer details (name and phone are required)
4. Tap the save button

### Recording Measurements
1. Select a customer from the list
2. Navigate to the Measurements tab
3. Tap the '+' button
4. Enter measurements (all fields are optional)
5. Save the measurements

### Creating an Order
1. Select a customer from the list
2. Navigate to the Orders tab
3. Tap the '+' button
4. Fill in order details (item type and price are required)
5. Set the due date
6. Enter advance payment if any
7. Save the order

### Managing Orders
- View all orders by tapping the orders icon in the customer list
- Filter orders by status using the filter chips
- Update order status by long-pressing an order
- View remaining balance calculations automatically

## Features in Detail

### Search Functionality
The customer search feature searches through both names and phone numbers, making it easy to find customers quickly.

### Automatic Balance Calculation
The app automatically calculates and displays the remaining balance for each order based on the total price and advance payment.

### Due Date Tracking
Orders with due dates in the past are highlighted in red (unless they are marked as completed or delivered).

### Data Persistence
All data is stored locally using Room database, ensuring data is available offline and persists across app restarts.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Support

For issues, questions, or suggestions, please open an issue on GitHub.

## Acknowledgments

- Built with Jetpack Compose
- Material 3 Design
- Room Persistence Library
