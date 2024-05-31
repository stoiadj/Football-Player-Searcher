import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Random;

public class FootballDash extends JFrame {

    private Connection connect = null;
    private PreparedStatement preparedStatement = null;
    private ResultSet resultSet = null;

    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextArea resultsArea;

    private JTextField newPlayerIdField;
    private JTextField newFirstNameField;
    private JTextField newLastNameField;
    private JTextField newClubNameField;
    private JTextField newSubPositionField;
    private JTextField newMarketValueField;

    public FootballDash() {
        super("Football Player Dashboard");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(3, 2));
        add(inputPanel, BorderLayout.NORTH);

        inputPanel.add(new JLabel("First Name:"));
        firstNameField = new JTextField(20);
        inputPanel.add(firstNameField);
        inputPanel.add(new JLabel("Last Name:"));
        lastNameField = new JTextField(20);
        inputPanel.add(lastNameField);

        JButton searchButton = new JButton("Search");
        inputPanel.add(searchButton);

        resultsArea = new JTextArea(10, 30);
        resultsArea.setEditable(false);
        add(new JScrollPane(resultsArea), BorderLayout.CENTER);

        JPanel addPlayerPanel = new JPanel();
        addPlayerPanel.setLayout(new GridLayout(7, 2));
        add(new JScrollPane(addPlayerPanel), BorderLayout.SOUTH);

        addPlayerPanel.add(new JLabel("Player ID (optional):"));
        newPlayerIdField = new JTextField(20);
        addPlayerPanel.add(newPlayerIdField);
        addPlayerPanel.add(new JLabel("First Name:"));
        newFirstNameField = new JTextField(20);
        addPlayerPanel.add(newFirstNameField);
        addPlayerPanel.add(new JLabel("Last Name:"));
        newLastNameField = new JTextField(20);
        addPlayerPanel.add(newLastNameField);
        addPlayerPanel.add(new JLabel("Club Name:"));
        newClubNameField = new JTextField(20);
        addPlayerPanel.add(newClubNameField);
        addPlayerPanel.add(new JLabel("Sub-Position:"));
        newSubPositionField = new JTextField(20);
        addPlayerPanel.add(newSubPositionField);
        addPlayerPanel.add(new JLabel("Market Value (Euros):"));
        newMarketValueField = new JTextField(20);
        addPlayerPanel.add(newMarketValueField);

        JButton addButton = new JButton("Add Player");
        addPlayerPanel.add(addButton);

        searchButton.addActionListener(e -> searchPlayers());
        addButton.addActionListener(e -> addPlayer());
    }

    private void searchPlayers() {
        try {
            String username = "";
            String password = "";
            connect = DriverManager.getConnection("jdbc:mysql://localhost/Football?" + "user=" + username + "&password=" + password);

            String query = "SELECT p.first_name, p.last_name, c.club_name, p.sub_position, pc.market_value_in_eur, a.avg_transfer_fee " +
                           "FROM Players p " +
                           "JOIN PlayerClubContracts pc ON p.player_id = pc.player_id " +
                           "JOIN Clubs c ON pc.club_id = c.club_id " +
                           "LEFT JOIN (SELECT club_id, AVG(market_value_in_eur) as avg_transfer_fee FROM PlayerClubContracts GROUP BY club_id) a ON c.club_id = a.club_id " +
                           "WHERE p.first_name = ? AND p.last_name = ?";
            preparedStatement = connect.prepareStatement(query);
            preparedStatement.setString(1, firstNameField.getText());
            preparedStatement.setString(2, lastNameField.getText());
            resultSet = preparedStatement.executeQuery();

            resultsArea.setText("");
            if (!resultSet.isBeforeFirst()) {
                resultsArea.setText("No results found.");
                return;
            }
            while (resultSet.next()) {
                double marketValue = resultSet.getDouble("market_value_in_eur");
                double avgTransferFee = resultSet.getDouble("avg_transfer_fee");
                String formattedMarketValue = String.format("%,.2f", marketValue);
                String formattedAvgTransferFee = String.format("%,.2f", avgTransferFee);

                resultsArea.append("First Name: " + resultSet.getString("first_name") +
                                   ", Last Name: " + resultSet.getString("last_name") +
                                   ", Club: " + resultSet.getString("club_name") +
                                   ", Sub-Position: " + resultSet.getString("sub_position") +
                                   ", Market Value (Euros): " + formattedMarketValue +
                                   ", Average Club Transfer Fee (Euros): " + formattedAvgTransferFee + "\n");
            }
        } catch (SQLException e) {
            resultsArea.setText("Database error: " + e.getMessage());
        } finally {
        	
        }
    }
    private void addPlayer() {
        Connection connect = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {

            String username = "";
            String password = "";
            connect = DriverManager.getConnection("jdbc:mysql://localhost/Football?" + "user=" + username + "&password=" + password);
            connect.setAutoCommit(false);

            int playerId = !newPlayerIdField.getText().trim().isEmpty() ? Integer.parseInt(newPlayerIdField.getText()) : generateUniqueId(); 
            String query = "INSERT INTO Players (player_id, first_name, last_name, sub_position) VALUES (?, ?, ?, ?)";
            preparedStatement = connect.prepareStatement(query);
            preparedStatement.setInt(1, playerId);
            preparedStatement.setString(2, newFirstNameField.getText());
            preparedStatement.setString(3, newLastNameField.getText());
            preparedStatement.setString(4, newSubPositionField.getText());
            preparedStatement.executeUpdate();

            query = "SELECT club_id FROM Clubs WHERE club_name = ?";
            preparedStatement = connect.prepareStatement(query);
            preparedStatement.setString(1, newClubNameField.getText());
            resultSet = preparedStatement.executeQuery();
            int clubId = resultSet.next() ? resultSet.getInt("club_id") : 0;

            int contractId = generateUniqueId();

            query = "INSERT INTO PlayerClubContracts (contract_id, player_id, club_id, market_value_in_eur) VALUES (?, ?, ?, ?)";
            preparedStatement = connect.prepareStatement(query);
            preparedStatement.setInt(1, contractId);
            preparedStatement.setInt(2, playerId);
            preparedStatement.setInt(3, clubId);
            preparedStatement.setDouble(4, Double.parseDouble(newMarketValueField.getText()));
            preparedStatement.executeUpdate();

            connect.commit();
            resultsArea.setText("New player added successfully.");
        } catch (SQLException e) {
            if (connect != null) {
                try {
                    connect.rollback();
                } catch (SQLException ex) {
                    resultsArea.setText("Database error on rollback: " + ex.getMessage());
                }
            }
            resultsArea.setText("Database error: " + e.getMessage());
        } finally {
            closeResources(connect, preparedStatement, resultSet);
        }
    }

    private int generateUniqueId() {
        return new Random().nextInt(1000000);
    }

    private void closeResources(Connection connect, PreparedStatement preparedStatement, ResultSet resultSet) {
        try {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
            if (connect != null) connect.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                FootballDash frame = new FootballDash();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
} 