package jdbcproject;

import java.sql.*;
import java.util.Scanner;
import java.time.LocalDate;

public class Crud {

    public static void add_transaction(int userId) {
        try {
            Connection conn = Tracker.conn;
            Scanner sc = new Scanner(System.in);

            String sql = "INSERT INTO expenses (user_id, date, amount, category_id, description) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pmst = conn.prepareStatement(sql);

            pmst.setInt(1, userId);  // Use user_id from logged-in user

            pmst.setDate(2, Date.valueOf(LocalDate.now())); // current date

            System.out.print("Enter amount: ");
            pmst.setDouble(3, sc.nextDouble());

            System.out.println("Enter category ID (1.Food 2.Travel 3.Rent 4.Other Bills): ");
            pmst.setInt(4, sc.nextInt());

            System.out.print("Enter description: ");
            sc.nextLine(); // consume newline
            String desc = sc.nextLine();
            pmst.setString(5, desc);

            int i = pmst.executeUpdate();
            if (i > 0) {
                System.out.println("Transaction inserted successfully!");
            } else {
                System.out.println("Failed to insert transaction.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error adding transaction.");
        }
    }

    public static void delete_transaction(int userId) {
        try {
            Connection conn = Tracker.conn;
            Scanner sc = new Scanner(System.in);

            System.out.print("Enter expense ID to delete: ");
            int expenseId = sc.nextInt();

            String sql = "DELETE FROM expenses WHERE expense_id = ? AND user_id = ?";
            PreparedStatement pmst = conn.prepareStatement(sql);
            pmst.setInt(1, expenseId);
            pmst.setInt(2, userId);

            int i = pmst.executeUpdate();
            if (i > 0) {
                System.out.println("Transaction deleted successfully!");
            } else {
                System.out.println("Transaction not found.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error deleting transaction.");
        }
    }

    public static void update_transaction(int userId) {
        try {
            Connection conn = Tracker.conn;
            Scanner sc = new Scanner(System.in);

            System.out.print("Enter expense ID to update: ");
            int expenseId = sc.nextInt();

            System.out.print("Enter new amount: ");
            double amount = sc.nextDouble();

            System.out.print("Enter new category ID (1.Food 2.Travel 3.Rent 4.Other Bills): ");
            int categoryId = sc.nextInt();

            System.out.print("Enter new description: ");
            sc.nextLine(); // clear newline
            String description = sc.nextLine();

            String sql = "UPDATE expenses SET amount = ?, category_id = ?, description = ? WHERE expense_id = ? AND user_id = ?";
            PreparedStatement pmst = conn.prepareStatement(sql);
            pmst.setDouble(1, amount);
            pmst.setInt(2, categoryId);
            pmst.setString(3, description);
            pmst.setInt(4, expenseId);
            pmst.setInt(5, userId);

            int i = pmst.executeUpdate();
            if (i > 0) {
                System.out.println("Transaction updated successfully!");
            } else {
                System.out.println("Transaction not found.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error updating transaction.");
        }
    }

    public static void generate_report(int userId) {
        try {
            Connection conn = Tracker.conn;
            Scanner sc = new Scanner(System.in);

            System.out.println("\nSelect Report Type:");
            System.out.println("1. Daily");
            System.out.println("2. Weekly");
            System.out.println("3. Monthly");
            System.out.println("4. Yearly");
            System.out.println("5. All");
            System.out.print("Enter your choice: ");
            int choice = sc.nextInt();

            String dateCondition = "";
            switch (choice) {
                case 1:
                    dateCondition = "AND DATE(date) = CURDATE()";
                    break;
                case 2:
                    dateCondition = "AND YEARWEEK(date, 1) = YEARWEEK(CURDATE(), 1)";
                    break;
                case 3:
                    dateCondition = "AND MONTH(date) = MONTH(CURDATE()) AND YEAR(date) = YEAR(CURDATE())";
                    break;
                case 4:
                    dateCondition = "AND YEAR(date) = YEAR(CURDATE())";
                    break;
                case 5:
                    dateCondition = ""; // no filter
                    break;
                default:
                    System.out.println("Invalid choice.");
                    return;
            }

            String sql = "SELECT e.expense_id, e.date, e.amount, c.category_name, e.description " +
                         "FROM expenses e JOIN categories c ON e.category_id = c.category_id " +
                         "WHERE e.user_id = ? " + dateCondition;

            PreparedStatement pmst = conn.prepareStatement(sql);
            pmst.setInt(1, userId);
            ResultSet rs = pmst.executeQuery();

            double total = 0;
            System.out.println("\n--- Expense Report ---");
            System.out.printf("%-10s %-12s %-10s %-15s %s\n", "ID", "Date", "Amount", "Category", "Description");
            System.out.println("----------------------------------------------------------------");

            while (rs.next()) {
                int id = rs.getInt("expense_id");
                Date date = rs.getDate("date");
                double amount = rs.getDouble("amount");
                String category = rs.getString("category_name");
                String desc = rs.getString("description");

                total += amount;
                System.out.printf("%-10d %-12s %-10.2f %-15s %s\n", id, date, amount, category, desc);
            }

            System.out.println("----------------------------------------------------------------");
            System.out.printf("Total Expense: ₹%.2f\n", total);

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error generating report.");
        }
    }
}
