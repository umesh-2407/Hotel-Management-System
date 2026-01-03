import java.sql.*;
import java.util.Scanner;

public class HotelManagementSystem {
    private static Connection connection;
    private static Scanner scanner = new Scanner(System.in);
    private static String loggedInRole = "";
    
    // Database configuration
    private static final String DB_URL = "jdbc:mysql://localhost:3306/hotel_management";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";
    
    public static void main(String[] args) {
        try {
            // Load MySQL Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Connect to database
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("==============================================");
            System.out.println("   HOTEL MANAGEMENT SYSTEM");
            System.out.println("==============================================\n");
            
            // Login
            if (login()) {
                boolean running = true;
                while (running) {
                    displayMenu();
                    int choice = getIntInput("Enter choice: ");
                    
                    switch (choice) {
                        case 1: viewAvailableRooms(); break;
                        case 2: createBooking(); break;
                        case 3: viewBookings(); break;
                        case 4: checkInGuest(); break;
                        case 5: checkOutGuest(); break;
                        case 6: searchGuest(); break;
                        case 7: manageRooms(); break;
                        case 8: manageStaff(); break;
                        case 9: viewReports(); break;
                        case 0: 
                            running = false;
                            System.out.println("Thank you!");
                            break;
                        default: System.out.println("Invalid choice!");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) connection.close();
                scanner.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    // Login function
    private static boolean login() {
        System.out.println("--- LOGIN ---");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        
        try {
            Statement stmt = connection.createStatement();
            String query = "SELECT role, full_name FROM staff WHERE username = '" + username + 
                          "' AND password = '" + password + "' AND status = 'ACTIVE'";
            ResultSet rs = stmt.executeQuery(query);
            
            if (rs.next()) {
                loggedInRole = rs.getString("role");
                System.out.println("\nWelcome, " + rs.getString("full_name") + "!\n");
                return true;
            } else {
                System.out.println("Invalid credentials!");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Login error: " + e.getMessage());
            return false;
        }
    }
    
    // Display menu
    private static void displayMenu() {
        System.out.println("\n==============================================");
        System.out.println("              MAIN MENU");
        System.out.println("==============================================");
        System.out.println("1. View Available Rooms");
        System.out.println("2. Create Booking");
        System.out.println("3. View Bookings");
        System.out.println("4. Check-In Guest");
        System.out.println("5. Check-Out Guest");
        System.out.println("6. Search Guest");
        System.out.println("7. Manage Rooms");
        System.out.println("8. Manage Staff");
        System.out.println("9. View Reports");
        System.out.println("0. Exit");
        System.out.println("==============================================");
    }
    
    // View available rooms
    private static void viewAvailableRooms() {
        System.out.println("\n--- AVAILABLE ROOMS ---");
        
        try {
            Statement stmt = connection.createStatement();
            String query = "SELECT r.room_id, r.room_number, rt.type_name, rt.base_price, r.floor_number " +
                          "FROM rooms r JOIN room_types rt ON r.type_id = rt.type_id " +
                          "WHERE r.status = 'AVAILABLE'";
            ResultSet rs = stmt.executeQuery(query);
            
            System.out.println("\n+----------+-------------+---------------+----------+-------+");
            System.out.println("| Room ID  | Room Number | Type          | Price    | Floor |");
            System.out.println("+----------+-------------+---------------+----------+-------+");
            
            while (rs.next()) {
                System.out.printf("| %-8d | %-11s | %-13s | ₹%-7.2f | %-5d |\n",
                    rs.getInt("room_id"),
                    rs.getString("room_number"),
                    rs.getString("type_name"),
                    rs.getDouble("base_price"),
                    rs.getInt("floor_number"));
            }
            System.out.println("+----------+-------------+---------------+----------+-------+");
            
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    // Create booking
    private static void createBooking() {
        System.out.println("\n--- CREATE BOOKING ---");
        
        // Get guest details
        System.out.print("First Name: ");
        String firstName = scanner.nextLine();
        System.out.print("Last Name: ");
        String lastName = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Phone: ");
        String phone = scanner.nextLine();
        System.out.print("ID Proof: ");
        String idProof = scanner.nextLine();
        
        // Get booking details
        System.out.print("Check-in date (YYYY-MM-DD): ");
        String checkIn = scanner.nextLine();
        System.out.print("Check-out date (YYYY-MM-DD): ");
        String checkOut = scanner.nextLine();
        int roomId = getIntInput("Room ID: ");
        int numGuests = getIntInput("Number of guests: ");
        
        try {
            Statement stmt = connection.createStatement();
            
            // Get room price
            String priceQuery = "SELECT rt.base_price FROM rooms r " +
                               "JOIN room_types rt ON r.type_id = rt.type_id " +
                               "WHERE r.room_id = " + roomId;
            ResultSet rs = stmt.executeQuery(priceQuery);
            
            if (!rs.next()) {
                System.out.println("Invalid room ID!");
                return;
            }
            
            double price = rs.getDouble("base_price");
            double total = price * 1; // Simplified - 1 day
            
            // Insert guest
            String guestQuery = "INSERT INTO guests (first_name, last_name, email, phone, id_proof) " +
                               "VALUES ('" + firstName + "', '" + lastName + "', '" + email + 
                               "', '" + phone + "', '" + idProof + "')";
            stmt.executeUpdate(guestQuery, Statement.RETURN_GENERATED_KEYS);
            
            rs = stmt.getGeneratedKeys();
            int guestId = 0;
            if (rs.next()) {
                guestId = rs.getInt(1);
            }
            
            // Create booking
            String bookingQuery = "INSERT INTO bookings (guest_id, room_id, check_in_date, " +
                                 "check_out_date, num_guests, total_amount, booked_by) " +
                                 "VALUES (" + guestId + ", " + roomId + ", '" + checkIn + 
                                 "', '" + checkOut + "', " + numGuests + ", " + total + ", 1)";
            stmt.executeUpdate(bookingQuery, Statement.RETURN_GENERATED_KEYS);
            
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                System.out.println("\n✓ Booking created! ID: " + rs.getInt(1));
                System.out.println("Total Amount: ₹" + total);
            }
            
            // Update room status
            stmt.executeUpdate("UPDATE rooms SET status = 'OCCUPIED' WHERE room_id = " + roomId);
            
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    // View all bookings
    private static void viewBookings() {
        System.out.println("\n--- ALL BOOKINGS ---");
        
        try {
            Statement stmt = connection.createStatement();
            String query = "SELECT b.booking_id, CONCAT(g.first_name, ' ', g.last_name) AS guest_name, " +
                          "r.room_number, b.check_in_date, b.check_out_date, " +
                          "b.booking_status, b.total_amount " +
                          "FROM bookings b " +
                          "JOIN guests g ON b.guest_id = g.guest_id " +
                          "JOIN rooms r ON b.room_id = r.room_id " +
                          "ORDER BY b.booking_id DESC LIMIT 20";
            ResultSet rs = stmt.executeQuery(query);
            
            System.out.println("\n+------------+----------------------+-------------+------------+------------+--------------+----------+");
            System.out.println("| Booking ID | Guest Name           | Room        | Check-In   | Check-Out  | Status       | Amount   |");
            System.out.println("+------------+----------------------+-------------+------------+------------+--------------+----------+");
            
            while (rs.next()) {
                System.out.printf("| %-10d | %-20s | %-11s | %-10s | %-10s | %-12s | ₹%-7.2f |\n",
                    rs.getInt("booking_id"),
                    rs.getString("guest_name"),
                    rs.getString("room_number"),
                    rs.getString("check_in_date"),
                    rs.getString("check_out_date"),
                    rs.getString("booking_status"),
                    rs.getDouble("total_amount"));
            }
            System.out.println("+------------+----------------------+-------------+------------+------------+--------------+----------+");
            
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    // Check-in guest
    private static void checkInGuest() {
        System.out.println("\n--- CHECK-IN ---");
        int bookingId = getIntInput("Booking ID: ");
        
        try {
            Statement stmt = connection.createStatement();
            String query = "UPDATE bookings SET booking_status = 'CHECKED_IN' " +
                          "WHERE booking_id = " + bookingId + " AND booking_status = 'CONFIRMED'";
            int rows = stmt.executeUpdate(query);
            
            if (rows > 0) {
                System.out.println("✓ Guest checked in successfully!");
            } else {
                System.out.println("Booking not found or already checked in!");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    // Check-out guest
    private static void checkOutGuest() {
        System.out.println("\n--- CHECK-OUT ---");
        int bookingId = getIntInput("Booking ID: ");
        
        try {
            Statement stmt = connection.createStatement();
            
            // Get booking amount
            String query = "SELECT total_amount, room_id FROM bookings " +
                          "WHERE booking_id = " + bookingId + " AND booking_status = 'CHECKED_IN'";
            ResultSet rs = stmt.executeQuery(query);
            
            if (!rs.next()) {
                System.out.println("Booking not found!");
                return;
            }
            
            double amount = rs.getDouble("total_amount");
            int roomId = rs.getInt("room_id");
            
            System.out.println("Total Amount: ₹" + amount);
            System.out.print("Payment Method (CASH/CARD/UPI): ");
            String method = scanner.nextLine();
            
            // Insert payment
            String paymentQuery = "INSERT INTO payments (booking_id, amount, payment_method) " +
                                 "VALUES (" + bookingId + ", " + amount + ", '" + method + "')";
            stmt.executeUpdate(paymentQuery);
            
            // Update booking status
            stmt.executeUpdate("UPDATE bookings SET booking_status = 'CHECKED_OUT' " +
                             "WHERE booking_id = " + bookingId);
            
            // Update room status
            stmt.executeUpdate("UPDATE rooms SET status = 'AVAILABLE' WHERE room_id = " + roomId);
            
            System.out.println("✓ Check-out successful! Payment received: ₹" + amount);
            
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    // Search guest
    private static void searchGuest() {
        System.out.println("\n--- SEARCH GUEST ---");
        System.out.print("Enter phone or email: ");
        String search = scanner.nextLine();
        
        try {
            Statement stmt = connection.createStatement();
            String query = "SELECT g.guest_id, CONCAT(g.first_name, ' ', g.last_name) AS name, " +
                          "g.email, g.phone, COUNT(b.booking_id) AS bookings " +
                          "FROM guests g " +
                          "LEFT JOIN bookings b ON g.guest_id = b.guest_id " +
                          "WHERE g.phone LIKE '%" + search + "%' OR g.email LIKE '%" + search + "%' " +
                          "GROUP BY g.guest_id";
            ResultSet rs = stmt.executeQuery(query);
            
            System.out.println("\n+----------+----------------------+---------------------------+---------------+----------+");
            System.out.println("| Guest ID | Name                 | Email                     | Phone         | Bookings |");
            System.out.println("+----------+----------------------+---------------------------+---------------+----------+");
            
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("| %-8d | %-20s | %-25s | %-13s | %-8d |\n",
                    rs.getInt("guest_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getInt("bookings"));
            }
            System.out.println("+----------+----------------------+---------------------------+---------------+----------+");
            
            if (!found) System.out.println("No guest found!");
            
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    // View reports
    private static void viewReports() {
        System.out.println("\n--- REPORTS ---");
        System.out.println("1. Occupancy Report");
        System.out.println("2. Revenue Report");
        
        int choice = getIntInput("Choose: ");
        
        if (choice == 1) {
            occupancyReport();
        } else if (choice == 2) {
            revenueReport();
        }
    }
    
    // Manage rooms
    private static void manageRooms() {
        System.out.println("\n--- MANAGE ROOMS ---");
        System.out.println("1. Add New Room");
        System.out.println("2. Update Room Status");
        System.out.println("3. View All Rooms");
        
        int choice = getIntInput("Choose: ");
        
        if (choice == 1) {
            addNewRoom();
        } else if (choice == 2) {
            updateRoomStatus();
        } else if (choice == 3) {
            viewAllRooms();
        }
    }
    
    // Add new room
    private static void addNewRoom() {
        System.out.println("\n--- ADD NEW ROOM ---");
        System.out.print("Room Number: ");
        String roomNumber = scanner.nextLine();
        int typeId = getIntInput("Room Type (1-Single, 2-Double, 3-Deluxe, 4-Suite): ");
        int floor = getIntInput("Floor Number: ");
        
        try {
            Statement stmt = connection.createStatement();
            String query = "INSERT INTO rooms (room_number, type_id, floor_number, status) " +
                          "VALUES ('" + roomNumber + "', " + typeId + ", " + floor + ", 'AVAILABLE')";
            stmt.executeUpdate(query);
            System.out.println("✓ Room added successfully!");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    // Update room status
    private static void updateRoomStatus() {
        System.out.println("\n--- UPDATE ROOM STATUS ---");
        int roomId = getIntInput("Room ID: ");
        System.out.print("New Status (AVAILABLE/OCCUPIED/MAINTENANCE): ");
        String status = scanner.nextLine();
        
        try {
            Statement stmt = connection.createStatement();
            String query = "UPDATE rooms SET status = '" + status + "' WHERE room_id = " + roomId;
            int rows = stmt.executeUpdate(query);
            
            if (rows > 0) {
                System.out.println("✓ Room status updated!");
            } else {
                System.out.println("Room not found!");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    // View all rooms
    private static void viewAllRooms() {
        System.out.println("\n--- ALL ROOMS ---");
        
        try {
            Statement stmt = connection.createStatement();
            String query = "SELECT r.room_id, r.room_number, rt.type_name, r.floor_number, " +
                          "r.status, rt.base_price FROM rooms r " +
                          "JOIN room_types rt ON r.type_id = rt.type_id " +
                          "ORDER BY r.room_number";
            ResultSet rs = stmt.executeQuery(query);
            
            System.out.println("\n+----------+-------------+---------------+-------+-------------+----------+");
            System.out.println("| Room ID  | Room Number | Type          | Floor | Status      | Price    |");
            System.out.println("+----------+-------------+---------------+-------+-------------+----------+");
            
            while (rs.next()) {
                System.out.printf("| %-8d | %-11s | %-13s | %-5d | %-11s | ₹%-7.2f |\n",
                    rs.getInt("room_id"),
                    rs.getString("room_number"),
                    rs.getString("type_name"),
                    rs.getInt("floor_number"),
                    rs.getString("status"),
                    rs.getDouble("base_price"));
            }
            System.out.println("+----------+-------------+---------------+-------+-------------+----------+");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    // Manage staff
    private static void manageStaff() {
        System.out.println("\n--- MANAGE STAFF ---");
        System.out.println("1. Add New Staff");
        System.out.println("2. View All Staff");
        System.out.println("3. Update Staff Status");
        
        int choice = getIntInput("Choose: ");
        
        if (choice == 1) {
            addNewStaff();
        } else if (choice == 2) {
            viewAllStaff();
        } else if (choice == 3) {
            updateStaffStatus();
        }
    }
    
    // Add new staff
    private static void addNewStaff() {
        System.out.println("\n--- ADD NEW STAFF ---");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("Full Name: ");
        String fullName = scanner.nextLine();
        System.out.print("Role (ADMIN/RECEPTIONIST/MANAGER): ");
        String role = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Phone: ");
        String phone = scanner.nextLine();
        
        try {
            Statement stmt = connection.createStatement();
            String query = "INSERT INTO staff (username, password, full_name, role, email, phone, status) " +
                          "VALUES ('" + username + "', '" + password + "', '" + fullName + "', '" + 
                          role + "', '" + email + "', '" + phone + "', 'ACTIVE')";
            stmt.executeUpdate(query);
            System.out.println("✓ Staff added successfully!");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    // View all staff
    private static void viewAllStaff() {
        System.out.println("\n--- ALL STAFF ---");
        
        try {
            Statement stmt = connection.createStatement();
            String query = "SELECT staff_id, username, full_name, role, email, phone, status " +
                          "FROM staff ORDER BY staff_id";
            ResultSet rs = stmt.executeQuery(query);
            
            System.out.println("\n+----------+-------------+----------------------+--------------+---------------------------+---------------+----------+");
            System.out.println("| Staff ID | Username    | Full Name            | Role         | Email                     | Phone         | Status   |");
            System.out.println("+----------+-------------+----------------------+--------------+---------------------------+---------------+----------+");
            
            while (rs.next()) {
                System.out.printf("| %-8d | %-11s | %-20s | %-12s | %-25s | %-13s | %-8s |\n",
                    rs.getInt("staff_id"),
                    rs.getString("username"),
                    rs.getString("full_name"),
                    rs.getString("role"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("status"));
            }
            System.out.println("+----------+-------------+----------------------+--------------+---------------------------+---------------+----------+");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    // Update staff status
    private static void updateStaffStatus() {
        System.out.println("\n--- UPDATE STAFF STATUS ---");
        int staffId = getIntInput("Staff ID: ");
        System.out.print("New Status (ACTIVE/INACTIVE): ");
        String status = scanner.nextLine();
        
        try {
            Statement stmt = connection.createStatement();
            String query = "UPDATE staff SET status = '" + status + "' WHERE staff_id = " + staffId;
            int rows = stmt.executeUpdate(query);
            
            if (rows > 0) {
                System.out.println("✓ Staff status updated!");
            } else {
                System.out.println("Staff not found!");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    // Occupancy report
    private static void occupancyReport() {
        System.out.println("\n--- OCCUPANCY REPORT ---");
        
        try {
            Statement stmt = connection.createStatement();
            String query = "SELECT rt.type_name, " +
                          "COUNT(r.room_id) AS total, " +
                          "SUM(CASE WHEN r.status = 'OCCUPIED' THEN 1 ELSE 0 END) AS occupied, " +
                          "SUM(CASE WHEN r.status = 'AVAILABLE' THEN 1 ELSE 0 END) AS available " +
                          "FROM room_types rt " +
                          "LEFT JOIN rooms r ON rt.type_id = r.type_id " +
                          "GROUP BY rt.type_id";
            ResultSet rs = stmt.executeQuery(query);
            
            System.out.println("\n+---------------+-------+----------+-----------+");
            System.out.println("| Room Type     | Total | Occupied | Available |");
            System.out.println("+---------------+-------+----------+-----------+");
            
            int totalRooms = 0, totalOccupied = 0;
            
            while (rs.next()) {
                int total = rs.getInt("total");
                int occupied = rs.getInt("occupied");
                int available = rs.getInt("available");
                
                System.out.printf("| %-13s | %-5d | %-8d | %-9d |\n",
                    rs.getString("type_name"), total, occupied, available);
                
                totalRooms += total;
                totalOccupied += occupied;
            }
            System.out.println("+---------------+-------+----------+-----------+");
            
            double rate = totalRooms > 0 ? (totalOccupied * 100.0 / totalRooms) : 0;
            System.out.printf("Occupancy Rate: %.2f%%\n", rate);
            
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    // Revenue report
    private static void revenueReport() {
        System.out.println("\n--- REVENUE REPORT ---");
        
        try {
            Statement stmt = connection.createStatement();
            String query = "SELECT " +
                          "COUNT(*) AS bookings, " +
                          "SUM(total_amount) AS revenue, " +
                          "AVG(total_amount) AS avg_amount " +
                          "FROM bookings " +
                          "WHERE booking_status != 'CANCELLED'";
            ResultSet rs = stmt.executeQuery(query);
            
            if (rs.next()) {
                System.out.println("\n========================================");
                System.out.println("Total Bookings: " + rs.getInt("bookings"));
                System.out.println("Total Revenue: ₹" + rs.getDouble("revenue"));
                System.out.println("Average Booking: ₹" + String.format("%.2f", rs.getDouble("avg_amount")));
                System.out.println("========================================");
            }
            
            // Revenue by room type
            String typeQuery = "SELECT rt.type_name, " +
                              "COUNT(b.booking_id) AS bookings, " +
                              "SUM(b.total_amount) AS revenue " +
                              "FROM bookings b " +
                              "JOIN rooms r ON b.room_id = r.room_id " +
                              "JOIN room_types rt ON r.type_id = rt.type_id " +
                              "WHERE b.booking_status != 'CANCELLED' " +
                              "GROUP BY rt.type_id";
            rs = stmt.executeQuery(typeQuery);
            
            System.out.println("\nBy Room Type:");
            System.out.println("+---------------+----------+------------+");
            System.out.println("| Type          | Bookings | Revenue    |");
            System.out.println("+---------------+----------+------------+");
            
            while (rs.next()) {
                System.out.printf("| %-13s | %-8d | ₹%-9.2f |\n",
                    rs.getString("type_name"),
                    rs.getInt("bookings"),
                    rs.getDouble("revenue"));
            }
            System.out.println("+---------------+----------+------------+");
            
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    // Helper method for integer input
    private static int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number!");
            }
        }
    }
}