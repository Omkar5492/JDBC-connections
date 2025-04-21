package jdbcproject;

import java.sql.*;
import java.util.Scanner;

public class Tracker {
    public static String driver = "com.mysql.cj.jdbc.Driver";
    public static String duser = "root";
    public static String dpassword = "root";
    public static String durl = "jdbc:mysql://localhost:3307/dbuse";
    public static Connection conn;
    public static boolean isLoggedIn = false;
    public static String loggedInUsername = null;
    public static int loggedInUserId = -1;  // Store user_id

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        Class.forName(driver);
        conn = DriverManager.getConnection(durl, duser, dpassword);
        Scanner sc = new Scanner(System.in);

        while (true) {
            if (isLoggedIn) {
                displayLoggedInMenu(sc);
            } else {
                displayMainMenu(sc);
            }
        }
    }

    private static void displayMainMenu(Scanner sc) {
        System.out.println("\nChoose an action:");
        System.out.println("1. Signup");
        System.out.println("2. Login");
        System.out.println("3. Exit");
        System.out.print("Enter your choice: ");
        String choice = sc.nextLine();

        switch (choice) {
            case "1":
                signup(sc);
                break;
            case "2":
                login(sc);
                break;
            case "3":
                System.out.println("Exiting...");
                sc.close();
                System.exit(0);
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    private static void displayLoggedInMenu(Scanner sc) throws ClassNotFoundException, SQLException {
        System.out.println("\n--- Logged In Menu ---");
        System.out.println("Welcome, " + loggedInUsername + " (User ID: " + loggedInUserId + ")");
        System.out.println("1. Add Transaction");
        System.out.println("2. Delete Transaction");
        System.out.println("3. Update Transaction");
        System.out.println("4. Generate Report");
        System.out.println("5. Logout");
        System.out.print("Enter your choice: ");
        String choice = sc.nextLine();

        switch (choice) {
            case "1":
                Crud.add_transaction(loggedInUserId);  // Pass the logged-in user's ID to Crud
                break;
            case "2":
                Crud.delete_transaction(loggedInUserId);  // Pass the logged-in user's ID to Crud
                break;
            case "3":
                Crud.update_transaction(loggedInUserId);  // Pass the logged-in user's ID to Crud
                break;
            case "4":
                Crud.generate_report(loggedInUserId);  // Pass the logged-in user's ID to Crud
                break;
            case "5":
                logout();
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    private static void logout() {
        isLoggedIn = false;
        loggedInUsername = null;
        loggedInUserId = -1;  // Reset user_id on logout
        System.out.println("Logged out successfully.");
    }

    private static void login(Scanner sc) {
        System.out.println("\n--- Login ---");
        System.out.print("Enter username: ");
        String username = sc.nextLine();
        System.out.print("Enter password: ");
        String password = sc.nextLine();

        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("Username and password are required.");
            return;
        }

        try {
            String query = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                isLoggedIn = true;
                loggedInUsername = resultSet.getString("username");
                loggedInUserId = resultSet.getInt("user_id");  // Set user_id on login
                System.out.println("Login successful!");
            } else {
                System.out.println("Invalid username or password.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error during login: " + e.getMessage());
        }
    }

    private static void signup(Scanner sc) {
        System.out.println("\n--- Signup ---");
        System.out.print("Enter username: ");
        String username = sc.nextLine();
        System.out.print("Enter password: ");
        String password = sc.nextLine();
        System.out.print("Enter email: ");
        String email = sc.nextLine();

        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            System.out.println("All fields are required.");
            return;
        }

        try {
            String checkQuery = "SELECT COUNT(*) FROM users WHERE username = ? OR email = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, username);
            checkStmt.setString(2, email);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            if (count > 0) {
                System.out.println("Username or email already exists.");
                return;
            }

            PreparedStatement preparedStatement = conn.prepareStatement(
                "INSERT INTO users (username, password, email) VALUES (?, ?, ?)");
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            preparedStatement.setString(3, email);

            int i = preparedStatement.executeUpdate();
            if (i > 0) {
                System.out.println("Signup successful!");
            } else {
                System.out.println("Signup failed.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error during signup: " + e.getMessage());
        }
    }
}
