import java.sql.*;
import java.util.*;

class Medicine {
    String name;
    String disease;
    int quantity;
    String expiryDate;

    public Medicine(String name, String disease, int quantity, String expiryDate) {
        this.name = name;
        this.disease = disease;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
    }

    @Override
    public String toString() {
        return name + " | Expiry: " + expiryDate + " | Quantity: " + quantity;
    }
}

public class PharmacyStore {
    static final String DB_URL = "jdbc:mysql://localhost:3306/pharmacy";
    static final String USER = "root";
    static final String PASS = "soham2005";
    static Scanner sc = new Scanner(System.in);

    public static Connection connectDB() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    public static void createTable() {
        try (Connection conn = connectDB();
             Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS medicines ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "name VARCHAR(100),"
                    + "disease VARCHAR(100),"
                    + "quantity INT,"
                    + "expiry_date VARCHAR(10))";
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addMedicine() {
        System.out.print("Enter medicine name: ");
        String name = sc.nextLine();

        System.out.print("Enter disease: ");
        String disease = sc.nextLine();

        System.out.print("Enter quantity: ");
        int quantity = sc.nextInt();
        sc.nextLine();

        System.out.print("Enter expiry date (YYYY-MM): ");
        String expiry = sc.nextLine();

        try (Connection conn = connectDB();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO medicines (name, disease, quantity, expiry_date) VALUES (?, ?, ?, ?)");) {
            pstmt.setString(1, name);
            pstmt.setString(2, disease);
            pstmt.setInt(3, quantity);
            pstmt.setString(4, expiry);
            pstmt.executeUpdate();
            System.out.println("Medicine added successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void displayDiseases() {
        try (Connection conn = connectDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT DISTINCT disease FROM medicines");) {
            List<String> diseases = new ArrayList<>();
            while (rs.next()) {
                diseases.add(rs.getString("disease"));
            }

            if (diseases.isEmpty()) {
                System.out.println("No diseases available.");
                return;
            }

            System.out.println("Available Diseases:");
            for (int i = 0; i < diseases.size(); i++) {
                System.out.println((i + 1) + ". " + diseases.get(i));
            }

            System.out.print("Select a disease: ");
            int choice = sc.nextInt();
            sc.nextLine();

            if (choice < 1 || choice > diseases.size()) {
                System.out.println("Invalid choice!");
                return;
            }

            displayMedicinesByDisease(diseases.get(choice - 1));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void displayMedicinesByDisease(String disease) {
        try (Connection conn = connectDB();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM medicines WHERE disease = ?");) {
            pstmt.setString(1, disease);
            ResultSet rs = pstmt.executeQuery();

            List<Medicine> medicines = new ArrayList<>();
            while (rs.next()) {
                medicines.add(new Medicine(rs.getString("name"), disease, rs.getInt("quantity"), rs.getString("expiry_date")));
            }

            if (medicines.isEmpty()) {
                System.out.println("No medicines available for this disease.");
                return;
            }

            for (Medicine med : medicines) {
                System.out.println(med);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void retrieveMedicine() {
        try (Connection conn = connectDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT DISTINCT disease FROM medicines")) {

            List<String> diseases = new ArrayList<>();
            while (rs.next()) {
                diseases.add(rs.getString("disease"));
            }

            if (diseases.isEmpty()) {
                System.out.println("No diseases available.");
                return;
            }

            System.out.println("Available Diseases:");
            for (int i = 0; i < diseases.size(); i++) {
                System.out.println((i + 1) + ". " + diseases.get(i));
            }

            System.out.print("Select a disease (enter number): ");
            if (!sc.hasNextInt()) {
                System.out.println("Invalid input! Please enter a number.");
                sc.nextLine(); // Consume incorrect input
                return;
            }
            int diseaseChoice = sc.nextInt();
            sc.nextLine();

            if (diseaseChoice < 1 || diseaseChoice > diseases.size()) {
                System.out.println("Invalid choice!");
                return;
            }

            String selectedDisease = diseases.get(diseaseChoice - 1);

            // Display available medicines for the selected disease
            List<Medicine> medicines = new ArrayList<>();
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM medicines WHERE disease = ?")) {
                pstmt.setString(1, selectedDisease);
                ResultSet medRs = pstmt.executeQuery();

                while (medRs.next()) {
                    medicines.add(new Medicine(
                            medRs.getString("name"),
                            selectedDisease,
                            medRs.getInt("quantity"),
                            medRs.getString("expiry_date")));
                }
            }

            if (medicines.isEmpty()) {
                System.out.println("No medicines available for this disease.");
                return;
            }

            System.out.println("Available Medicines:");
            for (int i = 0; i < medicines.size(); i++) {
                System.out.println((i + 1) + ". " + medicines.get(i));
            }

            System.out.print("Select a medicine (enter number): ");
            if (!sc.hasNextInt()) {
                System.out.println("Invalid input! Please enter a number.");
                sc.nextLine(); // Consume incorrect input
                return;
            }
            int medChoice = sc.nextInt();
            sc.nextLine();

            if (medChoice < 1 || medChoice > medicines.size()) {
                System.out.println("Invalid medicine choice!");
                return;
            }

            Medicine selectedMedicine = medicines.get(medChoice - 1);

            System.out.print("Enter quantity to take: ");
            if (!sc.hasNextInt()) {
                System.out.println("Invalid input! Please enter a valid number.");
                sc.nextLine();
                return;
            }
            int takeQuantity = sc.nextInt();
            sc.nextLine();

            if (takeQuantity <= 0 || takeQuantity > selectedMedicine.quantity) {
                System.out.println("Not enough stock available or invalid quantity!");
                return;
            }

            // Update the medicine stock
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "UPDATE medicines SET quantity = quantity - ? WHERE name = ? AND disease = ?")) {
                pstmt.setInt(1, takeQuantity);
                pstmt.setString(2, selectedMedicine.name);
                pstmt.setString(3, selectedDisease);
                int updated = pstmt.executeUpdate();

                if (updated > 0) {
                    System.out.println("Medicine retrieved successfully!");
                } else {
                    System.out.println("Failed to retrieve medicine!");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    public static void main(String[] args) {
        createTable();
        while (true) {
            System.out.println("\nPharmacy Store Management");
            System.out.println("1. Add Medicine");
            System.out.println("2. View Medicines");
            System.out.println("3. Retrieve Medicine");
            System.out.println("4. Exit");
            System.out.print("Choose an option: ");

            int option = sc.nextInt();
            sc.nextLine();

            switch (option) {
                case 1:
                    addMedicine();
                    break;
                case 2:
                    displayDiseases();
                    break;
                case 3:
                    retrieveMedicine();
                    break;
                case 4:
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("Invalid option!");
            }
        }
    }
}
