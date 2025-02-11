import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ExpenseTracker {

    private static final String DB_URL = "jdbc:sqlite:expenses.db";
    private JFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField categoryField, amountField, dateField;

    public ExpenseTracker() {
        initializeDatabase();
        initializeUI();
    }

    private void initializeDatabase() {
        try (Connection connection = DriverManager.getConnection(DB_URL);
             Statement statement = connection.createStatement()) {
            String createTableQuery = "CREATE TABLE IF NOT EXISTS expenses (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "category TEXT NOT NULL, " +
                    "amount REAL NOT NULL, " +
                    "date TEXT NOT NULL)";
            statement.execute(createTableQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initializeUI() {
        frame = new JFrame("Expense Tracker");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        // Digital Clock
        JLabel clockLabel = new JLabel();
        clockLabel.setFont(new Font("Arial", Font.BOLD, 24));
        clockLabel.setHorizontalAlignment(SwingConstants.CENTER);
        Timer clockTimer = new Timer(1000, e -> {
            String currentTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
            clockLabel.setText("Current Time: " + currentTime);
        });
        clockTimer.start();
        frame.add(clockLabel, BorderLayout.NORTH);

        // Table for expenses
        tableModel = new DefaultTableModel(new String[]{"ID", "Category", "Amount", "Date"}, 0);
        table = new JTable(tableModel);
        loadExpenses();
        frame.add(new JScrollPane(table), BorderLayout.CENTER);

        // Input Panel
        JPanel inputPanel = new JPanel(new GridLayout(2, 4));

        categoryField = new JTextField();
        amountField = new JTextField();
        dateField = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        JButton addButton = new JButton("Add Expense");

        inputPanel.add(new JLabel("Category:"));
        inputPanel.add(categoryField);
        inputPanel.add(new JLabel("Amount:"));
        inputPanel.add(amountField);
        inputPanel.add(new JLabel("Date (YYYY-MM-DD):"));
        inputPanel.add(dateField);
        inputPanel.add(new JLabel());
        inputPanel.add(addButton);

        frame.add(inputPanel, BorderLayout.SOUTH);

        // Button Action
        addButton.addActionListener(e -> addExpense());

        frame.setVisible(true);
    }

    private void loadExpenses() {
        try (Connection connection = DriverManager.getConnection(DB_URL);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM expenses")) {

            tableModel.setRowCount(0); // Clear existing rows

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String category = resultSet.getString("category");
                double amount = resultSet.getDouble("amount");
                String date = resultSet.getString("date");
                tableModel.addRow(new Object[]{id, category, amount, date});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addExpense() {
        String category = categoryField.getText();
        String amountText = amountField.getText();
        String date = dateField.getText();

        if (category.isEmpty() || amountText.isEmpty() || date.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double amount = Double.parseDouble(amountText);

            try (Connection connection = DriverManager.getConnection(DB_URL);
                 PreparedStatement preparedStatement = connection.prepareStatement(
                         "INSERT INTO expenses (category, amount, date) VALUES (?, ?, ?)")
            ) {
                preparedStatement.setString(1, category);
                preparedStatement.setDouble(2, amount);
                preparedStatement.setString(3, date);
                preparedStatement.executeUpdate();
                loadExpenses();

                categoryField.setText("");
                amountField.setText("");
                dateField.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));

                JOptionPane.showMessageDialog(frame, "Expense added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Amount must be a valid number!", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ExpenseTracker::new);
    }
}
