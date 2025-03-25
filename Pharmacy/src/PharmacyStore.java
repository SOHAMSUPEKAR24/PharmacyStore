import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class PharmacyStore {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/pharmacy";
    private static final String USER = "root";
    private static final String PASS = "soham2005";

    private JFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;

    public PharmacyStore() {
        frame = new JFrame("Pharmacy Store");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Using GridLayout for better visibility of buttons
        JPanel panel = new JPanel(new GridLayout(1, 5, 5, 5));

        JButton addButton = new JButton("Add Medicine");
        JButton viewButton = new JButton("View Medicines");
        JButton retrieveButton = new JButton("Retrieve Medicine");
        JButton deleteButton = new JButton("Delete Medicine");
        JButton searchButton = new JButton("Search Medicine");

        panel.add(addButton);
        panel.add(viewButton);
        panel.add(retrieveButton);
        panel.add(deleteButton);
        panel.add(searchButton);

        frame.add(panel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Disease", "Quantity", "Expiry"}, 0);
        table = new JTable(tableModel);
        frame.add(new JScrollPane(table), BorderLayout.CENTER);

        addButton.addActionListener(e -> addMedicine());
        viewButton.addActionListener(e -> loadMedicines());
        retrieveButton.addActionListener(e -> retrieveMedicine());
        deleteButton.addActionListener(e -> deleteMedicine());
        searchButton.addActionListener(e -> searchMedicine());

        frame.pack();
        frame.setVisible(true);
    }

    private Connection connectDB() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    private void addMedicine() {
        try {
            String name = JOptionPane.showInputDialog("Enter medicine name:");
            String disease = JOptionPane.showInputDialog("Enter disease:");
            String quantityStr = JOptionPane.showInputDialog("Enter quantity:");
            String expiry = JOptionPane.showInputDialog("Enter expiry date (YYYY-MM):");

            if (name.isEmpty() || disease.isEmpty() || quantityStr.isEmpty() || expiry.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int quantity = Integer.parseInt(quantityStr);

            try (Connection conn = connectDB();
                 PreparedStatement pstmt = conn.prepareStatement("INSERT INTO medicines (name, disease, quantity, expiry_date) VALUES (?, ?, ?, ?)")) {
                pstmt.setString(1, name);
                pstmt.setString(2, disease);
                pstmt.setInt(3, quantity);
                pstmt.setString(4, expiry);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(frame, "Medicine added successfully!");
                loadMedicines();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Invalid quantity! Please enter a number.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadMedicines() {
        tableModel.setRowCount(0);
        try (Connection conn = connectDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM medicines")) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{rs.getInt("id"), rs.getString("name"), rs.getString("disease"), rs.getInt("quantity"), rs.getString("expiry_date")});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void retrieveMedicine() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Please select a medicine to retrieve.");
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        int currentQuantity = (int) tableModel.getValueAt(selectedRow, 3);
        String quantityStr = JOptionPane.showInputDialog("Enter quantity to retrieve:");

        try {
            int quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0 || quantity > currentQuantity) {
                JOptionPane.showMessageDialog(frame, "Invalid quantity! Must be between 1 and " + currentQuantity);
                return;
            }

            try (Connection conn = connectDB();
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE medicines SET quantity = quantity - ? WHERE id = ?")) {
                pstmt.setInt(1, quantity);
                pstmt.setInt(2, id);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(frame, "Medicine retrieved successfully!");
                loadMedicines();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Invalid quantity! Please enter a number.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteMedicine() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Please select a medicine to delete.");
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete this medicine?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = connectDB();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM medicines WHERE id = ?")) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(frame, "Medicine deleted successfully!");
            loadMedicines();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void searchMedicine() {
        String searchQuery = JOptionPane.showInputDialog("Enter medicine name to search:");
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Search query cannot be empty.");
            return;
        }

        tableModel.setRowCount(0);
        try (Connection conn = connectDB();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM medicines WHERE name LIKE ?")) {
            pstmt.setString(1, "%" + searchQuery + "%");
            ResultSet rs = pstmt.executeQuery();

            boolean found = false;
            while (rs.next()) {
                tableModel.addRow(new Object[]{rs.getInt("id"), rs.getString("name"), rs.getString("disease"), rs.getInt("quantity"), rs.getString("expiry_date")});
                found = true;
            }

            if (!found) {
                JOptionPane.showMessageDialog(frame, "No medicines found with the given name.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new PharmacyStore();
    }
}
