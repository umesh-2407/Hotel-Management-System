import java.sql.*;
import java.util.Scanner;

public class HotelManagementSystem {
    private static Connection connection;
    private static Scanner scanner = new Scanner(System.in);
    private static String loggedInUser = "";
    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/hotel_management";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";
    
    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            
            System.out.println("==============================================");
            System.out.println("   HOTEL MANAGEMENT SYSTEM");
            System.out.println("==============================================\n");
            
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
        } finally {
            try {
                if (connection != null) connection.close();
                scanner.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    private static boolean login() {
        System.out.println("--- LOGIN ---");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        
        try {
            Statement stmt = connection.createStatement();
            String query = "SELECT username, full_name FROM staff WHERE username = '" + 
                          username + "' AND password = '" + password + "' AND status = 'ACTIVE'";
            ResultSet rs = stmt.executeQuery(query);
            
            if (rs.next()) {
                loggedInUser = rs.getString("username");
                System.out.println("\nWelcome, " + rs.getString("full_name") + "!\n");
                return true;
            } else {
                System.out.println("Invalid credentials!");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }
    
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
    
    private static void viewAvailableRooms() {
        System.out.println("\n--- AVAILABLE ROOMS ---");
        
        try {
            Statement stmt = connection.createStatement();
            String query = "SELECT room_id, room_number, room_type, price, floor_number, capacity " +
                          "FROM rooms WHERE status = 'AVAILABLE' ORDER BY room_number";
            ResultSet rs = stmt.executeQuery(query);
            
            System.out.println("\n+----------+-------------+---------------+----------+-------+----------+");
            System.out.println("| Room ID  | Room Number | Type          | Price    | Floor | Capacity |");
            System.out.println("+----------+-------------+---------------+----------+-------+----------+");
            
            while (rs.next()) {
                System.out.printf("| %-8d | %-11s | %-13s | ₹%-7.2f | %-5d | %-8d |\n",
                    rs.getInt("room_id"),
                    rs.getString("room_number"),
                    rs.getString("room_type"),
                    rs.getDouble("price"),
                    rs.getInt("floor_number"),
                    rs.getInt("capacity"));
            }
            System.out.println("+----------+-------------+---------------+----------+-------+----------+");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    private static void createBooking() {
        System.out.println("\n--- CREATE BOOKING ---");
        
        System.out.print("Guest Name: ");
        String guestName = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Phone: ");
        String phone = scanner.nextLine();
        System.out.print("ID Proof: ");
        String idProof = scanner.nextLine();
        
        System.out.print("Check-in (YYYY-MM-DD): ");
        String checkIn = scanner.nextLine();
        System.out.print("Check-out (YYYY-MM-DD): ");
        String checkOut = scanner.nextLine();
        
        int roomId = getIntInput("Room ID: ");
        int numGuests = getIntInput("Number of guests: ");
        System.out.print("Special requests: ");
        String requests = scanner.nextLine();
        
        try {
            Statement stmt = connection.createStatement();
            
            // Get room details
            String roomQuery = "SELECT room_number, price FROM rooms WHERE room_id = " + roomId;
            ResultSet rs = stmt.executeQuery(roomQuery);
            
            if (!rs.next()) {
                System.out.println("Room not found!");
                return;
            }
            
            String roomNumber = rs.getString("room_number");
            double price = rs.getDouble("price");
            double total = price; // Simplified - 1 day
            
            // Insert booking (guest info included)
            String query = "INSERT INTO bookings (guest_name, guest_email, guest_phone, guest_id_proof, " +
                          "room_id, room_number, check_in_date, check_out_date, num_guests, total_amount, " +
                          "special_requests, booked_by) VALUES ('" + 
                          guestName + "', '" + email + "', '" + phone + "', '" + idProof + "', " +
                          roomId + ", '" + roomNumber + "', '" + checkIn + "', '" + checkOut + "', " +
                          numGuests + ", " + total + ", '" + requests + "', '" + loggedInUser + "')";
            
            stmt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
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
    
    private static void viewBookings() {
        System.out.println("\n--- ALL BOOKINGS ---");
        
        try {
            Statement stmt = connection.createStatement();
            String query = "SELECT booking_id, guest_name, guest_phone, room_number, " +
                          "check_in_date, check_out_date, booking_status, total_amount " +
                          "FROM bookings ORDER BY booking_id DESC LIMIT 20";
            ResultSet rs = stmt.executeQuery(query);
            
            System.out.println("\n+------------+----------------------+---------------+-------------+------------+------------+--------------+----------+");
            System.out.println("| Booking ID | Guest Name           | Phone         | Room        | Check-In   | Check-Out  | Status       | Amount   |");
            System.out.println("+------------+----------------------+---------------+-------------+------------+------------+--------------+----------+");
            
            while (rs.next()) {
                System.out.printf("| %-10d | %-20s | %-13s | %-11s | %-10s | %-10s | %-12s | ₹%-7.2f |\n",
                    rs.getInt("booking_id"),
                    rs.getString("guest_name"),
                    rs.getString("guest_phone"),
                    rs.getString("room_number"),
                    rs.getString("check_in_date"),
                    rs.getString("check_out_date"),
                    rs.getString("booking_status"),
                    rs.getDouble("total_amount"));
            }
            System.out.println("+------------+----------------------+---------------+-------------+------------+------------+--------------+----------+");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    private static void checkInGuest() {
        System.out.println("\n--- CHECK-IN ---");
        int bookingId = getIntInput("Booking ID: ");
        
        try {
            Statement stmt = connection.createStatement();
            String query = "UPDATE bookings SET booking_status = 'CHECKED_IN' " +
                          "WHERE booking_id = " + bookingId + " AND booking_status = 'CONFIRMED'";
            int rows = stmt.executeUpdate(query);
            
            if (rows > 0) {
                System.out.println("✓ Guest checked in!");
            } else {
                System.out.println("Booking not found!");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    private static void checkOutGuest() {
        System.out.println("\n--- CHECK-OUT ---");
        int bookingId = getIntInput("Booking ID: ");
        
        try {
            Statement stmt = connection.createStatement();
            
            // Get booking details
            String query = "SELECT total_amount, room_id FROM bookings " +
                          "WHERE booking_id = " + bookingId + " AND booking_status = 'CHECKED_IN'";
            ResultSet rs = stmt.executeQuery(query);
            
            if (!rs.next()) {
                System.out.println("Booking not found!");
                return;
            }
            
            double amount = rs.getDouble("total_amount");
            int roomId = rs.getInt("room_id");
            
            System.out.println("Total: ₹" + amount);
            System.out.print("Payment Method (CASH/CARD/UPI): ");
            String method = scanner.nextLine();
            
            // Update booking with payment info
            String updateQuery = "UPDATE bookings SET booking_status = 'CHECKED_OUT', " +
                                "payment_method = '" + method + "', payment_status = 'PAID' " +
                                "WHERE booking_id = " + bookingId;
            stmt.executeUpdate(updateQuery);
            
            // Update room status
            stmt.executeUpdate("UPDATE rooms SET status = 'AVAILABLE' WHERE room_id = " + roomId);
            
            System.out.println("✓ Check-out successful!");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    private static void searchGuest() {
        System.out.println("\n--- SEARCH GUEST ---");
        System.out.print("Enter phone or name: ");
        String search = scanner.nextLine();
        
        try {
            Statement stmt = connection.createStatement();
            String query = "SELECT guest_name, guest_phone, guest_email, " +
                          "COUNT(*) AS total_bookings, SUM(total_amount) AS total_spent " +
                          "FROM bookings WHERE guest_phone LIKE '%" + search + 
                          "%' OR guest_name LIKE '%" + search + "%' GROUP BY guest_phone";
            ResultSet rs = stmt.executeQuery(query);
            
            System.out.println("\n+----------------------+---------------+---------------------------+----------+-------------+");
            System.out.println("| Name                 | Phone         | Email                     | Bookings | Total Spent |");
            System.out.println("+----------------------+---------------+---------------------------+----------+-------------+");
            
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("| %-20s | %-13s | %-25s | %-8d | ₹%-10.2f |\n",
                    rs.getString("guest_name"),
                    rs.getString("guest_phone"),
                    rs.getString("guest_email"),
                    rs.getInt("total_bookings"),
                    rs.getDouble("total_spent"));
            }
            System.out.println("+----------------------+---------------+---------------------------+----------+-------------+");
            
            if (!found) System.out.println("No guest found!");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    private static void manageRooms() {
        System.out.println("\n--- MANAGE ROOMS ---");
        System.out.println("1. Add Room");
        System.out.println("2. Update Status");
        System.out.println("3. View All");
        
        int choice = getIntInput("Choose: ");
        
        if (choice == 1) addRoom();
        else if (choice == 2) updateRoomStatus();
        else if (choice == 3) viewAllRooms();
    }
    
    private static void addRoom() {
        System.out.println("\n--- ADD ROOM ---");
        System.out.print("Room Number: ");
        String number = scanner.nextLine();
        System.out.print("Type (Single/Double/Deluxe/Suite): ");
        String type = scanner.nextLine();
        double price = getDoubleInput("Price: ");
        int capacity = getIntInput("Capacity: ");
        int floor = getIntInput("Floor: ");
        
        try {
            Statement stmt = connection.createStatement();
            String query = "INSERT INTO rooms (room_number, room_type, price, capacity, floor_number) " +
                          "VALUES ('" + number + "', '" + type + "', " + price + ", " + capacity + ", " + floor + ")";
            stmt.executeUpdate(query);
            System.out.println("✓ Room added!");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    private static void updateRoomStatus() {
        System.out.println("\n--- UPDATE ROOM ---");
        int roomId = getIntInput("Room ID: ");
        System.out.print("Status (AVAILABLE/OCCUPIED/MAINTENANCE): ");
        String status = scanner.nextLine();
        
        try {
            Statement stmt = connection.createStatement();
            String query = "UPDATE rooms SET status = '" + status + "' WHERE room_id = " + roomId;
            int rows = stmt.executeUpdate(query);
            
            if (rows > 0) System.out.println("✓ Updated!");
            else System.out.println("Room not found!");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    private static void viewAllRooms() {
        System.out.println("\n--- ALL ROOMS ---");
        
        try {
            Statement stmt = connection.createStatement();
            String query = "SELECT * FROM rooms ORDER BY room_number";
            ResultSet rs = stmt.executeQuery(query);
            
            System.out.println("\n+----------+-------------+---------------+----------+-------+----------+-------------+");
            System.out.println("| Room ID  | Room Number | Type          | Price    | Floor | Capacity | Status      |");
            System.out.println("+----------+-------------+---------------+----------+-------+----------+-------------+");
            
            while (rs.next()) {
                System.out.printf("| %-8d | %-11s | %-13s | ₹%-7.2f | %-5d | %-8d | %-11s |\n",
                    rs.getInt("room_id"),
                    rs.getString("room_number"),
                    rs.getString("room_type"),
                    rs.getDouble("price"),
                    rs.getInt("floor_number"),
                    rs.getInt("capacity"),
                    rs.getString("status"));
            }
            System.out.println("+----------+-------------+---------------+----------+-------+----------+-------------+");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    private static void manageStaff() {
        System.out.println("\n--- MANAGE STAFF ---");
        System.out.println("1. Add Staff");
        System.out.println("2. View All");
        System.out.println("3. Update Status");
        
        int choice = getIntInput("Choose: ");
        
        if (choice == 1) addStaff();
        else if (choice == 2) viewAllStaff();
        else if (choice == 3) updateStaffStatus();
    }
    
    private static void addStaff() {
        System.out.println("\n--- ADD STAFF ---");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("Full Name: ");
        String name = scanner.nextLine();
        System.out.print("Role (ADMIN/RECEPTIONIST/MANAGER): ");
        String role = scanner.nextLine();
        System.out.print("Phone: ");
        String phone = scanner.nextLine();
        
        try {
            Statement stmt = connection.createStatement();
            String query = "INSERT INTO staff (username, password, full_name, role, phone) " +
                          "VALUES ('" + username + "', '" + password + "', '" + name + "', '" + 
                          role + "', '" + phone + "')";
            stmt.executeUpdate(query);
            System.out.println("✓ Staff added!");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    private static void viewAllStaff() {
        System.out.println("\n--- ALL STAFF ---");
        
        try {
            Statement stmt = connection.createStatement();
            String query = "SELECT * FROM staff ORDER BY staff_id";
            ResultSet rs = stmt.executeQuery(query);
            
            System.out.println("\n+----------+-------------+----------------------+--------------+---------------+----------+");
            System.out.println("| Staff ID | Username    | Full Name            | Role         | Phone         | Status   |");
            System.out.println("+----------+-------------+----------------------+--------------+---------------+----------+");
            
            while (rs.next()) {
                System.out.printf("| %-8d | %-11s | %-20s | %-12s | %-13s | %-8s |\n",
                    rs.getInt("staff_id"),
                    rs.getString("username"),
                    rs.getString("full_name"),
                    rs.getString("role"),
                    rs.getString("phone"),
                    rs.getString("status"));
            }
            System.out.println("+----------+-------------+----------------------+--------------+---------------+----------+");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    private static void updateStaffStatus() {
        System.out.println("\n--- UPDATE STAFF ---");
        int staffId = getIntInput("Staff ID: ");
        System.out.print("Status (ACTIVE/INACTIVE): ");
        String status = scanner.nextLine();
        
        try {
            Statement stmt = connection.createStatement();
            String query = "UPDATE staff SET status = '" + status + "' WHERE staff_id = " + staffId;
            int rows = stmt.executeUpdate(query);
            
            if (rows > 0) System.out.println("✓ Updated!");
            else System.out.println("Staff not found!");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    private static void viewReports() {
        System.out.println("\n--- REPORTS ---");
        System.out.println("1. Occupancy");
        System.out.println("2. Revenue");
        
        int choice = getIntInput("Choose: ");
        
        if (choice == 1) occupancyReport();
        else if (choice == 2) revenueReport();
    }
    
    private static void occupancyReport() {
        System.out.println("\n--- OCCUPANCY ---");
        
        try {
            Statement stmt = connection.createStatement();
            String query = "SELECT * FROM room_occupancy";
            ResultSet rs = stmt.executeQuery(query);
            
            System.out.println("\n+---------------+-------+----------+-----------+");
            System.out.println("| Type          | Total | Occupied | Available |");
            System.out.println("+---------------+-------+----------+-----------+");
            
            int total = 0, occupied = 0;
            while (rs.next()) {
                int t = rs.getInt("total_rooms");
                int o = rs.getInt("occupied_rooms");
                int a = rs.getInt("available_rooms");
                
                System.out.printf("| %-13s | %-5d | %-8d | %-9d |\n",
                    rs.getString("room_type"), t, o, a);
                total += t;
                occupied += o;
            }
            System.out.println("+---------------+-------+----------+-----------+");
            
            double rate = total > 0 ? (occupied * 100.0 / total) : 0;
            System.out.printf("Rate: %.2f%%\n", rate);
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    private static void revenueReport() {
        System.out.println("\n--- REVENUE ---");
        
        try {
            Statement stmt = connection.createStatement();
            String query = "SELECT COUNT(*) AS bookings, SUM(total_amount) AS revenue, " +
                          "AVG(total_amount) AS avg FROM bookings WHERE payment_status = 'PAID'";
            ResultSet rs = stmt.executeQuery(query);
            
            if (rs.next()) {
                System.out.println("\nTotal Bookings: " + rs.getInt("bookings"));
                System.out.println("Total Revenue: ₹" + rs.getDouble("revenue"));
                System.out.println("Average: ₹" + String.format("%.2f", rs.getDouble("avg")));
            }
            
            // By room type
            String typeQuery = "SELECT r.room_type, COUNT(b.booking_id) AS bookings, " +
                              "SUM(b.total_amount) AS revenue FROM bookings b " +
                              "JOIN rooms r ON b.room_id = r.room_id " +
                              "WHERE b.payment_status = 'PAID' GROUP BY r.room_type";
            rs = stmt.executeQuery(typeQuery);
            
            System.out.println("\n+---------------+----------+------------+");
            System.out.println("| Type          | Bookings | Revenue    |");
            System.out.println("+---------------+----------+------------+");
            
            while (rs.next()) {
                System.out.printf("| %-13s | %-8d | ₹%-9.2f |\n",
                    rs.getString("room_type"),
                    rs.getInt("bookings"),
                    rs.getDouble("revenue"));
            }
            System.out.println("+---------------+----------+------------+");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    private static int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Enter a number!");
            }
        }
    }
    
    private static double getDoubleInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Double.parseDouble(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Enter a valid number!");
            }
        }
    }
}
