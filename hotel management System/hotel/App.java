package main.java.com.hotel;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.*;
// Will need to use a different date picker component

public class App {
    private static JFrame mainFrame;
    private static JPanel mainPanel;
    private static JPanel searchPanel;
    private static JPanel resultsPanel;
    private static ArrayList<Room> rooms;
    private static User currentUser;
    
    public static void main(String[] args) {
        // Initialize database connection
        try {
            DatabaseManager.initialize();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                "Failed to connect to database. Please make sure MongoDB is running.\nError: " + e.getMessage(),
                "Database Connection Error",
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        
        initializeRooms();
        SwingUtilities.invokeLater(() -> createAndShowGUI());
        
        // Add shutdown hook to close database connection
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                DatabaseManager.close();
            } catch (Exception e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }));
    }
    
    private static void initializeRooms() {
        rooms = new ArrayList<>();
        // Add sample rooms
        rooms.add(new Room("Deluxe Double Room", "Sea View", 2, 150.00, "King bed, Balcony, Free WiFi"));
        rooms.add(new Room("Superior Suite", "City View", 3, 250.00, "1 King bed + 1 Sofa bed, Living Room, Mini Kitchen"));
        rooms.add(new Room("Family Room", "Garden View", 4, 200.00, "2 Queen beds, Balcony, Free Breakfast"));
        rooms.add(new Room("Executive Suite", "Sea View", 2, 300.00, "King bed, Jacuzzi, Living Room"));
        rooms.add(new Room("Standard Twin Room", "City View", 2, 100.00, "2 Single beds, Basic Amenities"));
    }
    
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        mainFrame = new JFrame("Hotel Booking System");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        // Create main container
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Create header
        JPanel headerPanel = createHeaderPanel();
        
        // Create search panel
        searchPanel = createSearchPanel();
        
        // Create results panel
        resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(resultsPanel);
        scrollPane.setBorder(null);
        
        // Add components to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(searchPanel, BorderLayout.WEST);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        mainFrame.add(mainPanel);
        mainFrame.setVisible(true);
        
        // Initial search to populate results
        updateResults();
    }
    
    private static JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0, 53, 128));
        headerPanel.setPreferredSize(new Dimension(-1, 80));
        
        JLabel titleLabel = new JLabel("Hotel Booking System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(new EmptyBorder(0, 20, 0, 0));
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);
        
        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");
        styleButton(loginButton);
        styleButton(registerButton);
        
        loginButton.addActionListener(e -> {
            LoginDialog loginDialog = new LoginDialog(mainFrame);
            loginDialog.setVisible(true);
            
            if (loginDialog.isSucceeded()) {
                currentUser = loginDialog.getLoggedInUser();
                updateHeaderForLoggedInUser(rightPanel);
            }
        });
        
        registerButton.addActionListener(e -> {
            RegisterDialog registerDialog = new RegisterDialog(mainFrame);
            registerDialog.setVisible(true);
        });
        
        rightPanel.add(loginButton);
        rightPanel.add(registerButton);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(rightPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private static void updateHeaderForLoggedInUser(JPanel rightPanel) {
        rightPanel.removeAll();
        
        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser.getFullName());
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        
        JButton logoutButton = new JButton("Logout");
        styleButton(logoutButton);
        logoutButton.addActionListener(e -> {
            currentUser = null;
            rightPanel.removeAll();
            JButton loginButton = new JButton("Login");
            JButton registerButton = new JButton("Register");
            styleButton(loginButton);
            styleButton(registerButton);
            loginButton.addActionListener(evt -> {
                LoginDialog loginDialog = new LoginDialog(mainFrame);
                loginDialog.setVisible(true);
                if (loginDialog.isSucceeded()) {
                    currentUser = loginDialog.getLoggedInUser();
                    updateHeaderForLoggedInUser(rightPanel);
                }
            });
            registerButton.addActionListener(evt -> {
                RegisterDialog registerDialog = new RegisterDialog(mainFrame);
                registerDialog.setVisible(true);
            });
            rightPanel.add(loginButton);
            rightPanel.add(registerButton);
            rightPanel.revalidate();
            rightPanel.repaint();
        });
        
        rightPanel.add(welcomeLabel);
        rightPanel.add(logoutButton);
        rightPanel.revalidate();
        rightPanel.repaint();
    }
    
    private static JPanel createSearchPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(300, -1));
        
        // Date selection
        JTextField checkInDate = new JTextField();
        JTextField checkOutDate = new JTextField();
        
        // Guests selection
        SpinnerModel guestModel = new SpinnerNumberModel(2, 1, 6, 1);
        JSpinner guestsSpinner = new JSpinner(guestModel);
        
        // Price range
        JSlider priceSlider = new JSlider(JSlider.HORIZONTAL, 0, 500, 250);
        priceSlider.setMajorTickSpacing(100);
        priceSlider.setPaintTicks(true);
        priceSlider.setPaintLabels(true);
        
        // Room type selection
        String[] roomTypes = {"Any", "Standard", "Deluxe", "Suite"};
        JComboBox<String> roomTypeCombo = new JComboBox<>(roomTypes);
        
        // Add components with labels
        addLabelAndComponent(panel, "Check-in Date:", checkInDate);
        addLabelAndComponent(panel, "Check-out Date:", checkOutDate);
        addLabelAndComponent(panel, "Number of Guests:", guestsSpinner);
        addLabelAndComponent(panel, "Maximum Price ($):", priceSlider);
        addLabelAndComponent(panel, "Room Type:", roomTypeCombo);
        
        // Search button
        JButton searchButton = new JButton("Search");
        searchButton.setBackground(new Color(0, 113, 194));
        searchButton.setForeground(Color.WHITE);
        searchButton.setFont(new Font("Arial", Font.BOLD, 14));
        searchButton.addActionListener(e -> updateResults());
        
        panel.add(Box.createVerticalStrut(20));
        panel.add(searchButton);
        
        return panel;
    }
    
    private static void addLabelAndComponent(JPanel panel, String labelText, JComponent component) {
        panel.add(new JLabel(labelText));
        panel.add(Box.createVerticalStrut(5));
        panel.add(component);
        panel.add(Box.createVerticalStrut(15));
    }
    
    private static void styleButton(JButton button) {
        button.setBackground(new Color(0, 113, 194));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
    }
    
    private static void updateResults() {
        resultsPanel.removeAll();
        
        for (Room room : rooms) {
            JPanel roomPanel = createRoomPanel(room);
            resultsPanel.add(roomPanel);
            resultsPanel.add(Box.createVerticalStrut(10));
        }
        
        resultsPanel.revalidate();
        resultsPanel.repaint();
    }
    
    private static JPanel createRoomPanel(Room room) {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Color.LIGHT_GRAY),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        // Room image (placeholder)
        JPanel imagePanel = new JPanel();
        imagePanel.setPreferredSize(new Dimension(200, 150));
        imagePanel.setBackground(new Color(200, 200, 200));
        
        // Room details
        JPanel detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel(room.getName() + " - " + room.getView());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        
        JLabel descLabel = new JLabel("<html>" + room.getDescription() + "</html>");
        JLabel priceLabel = new JLabel(String.format("$%.2f per night", room.getPrice()));
        priceLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        detailsPanel.add(titleLabel, BorderLayout.NORTH);
        detailsPanel.add(descLabel, BorderLayout.CENTER);
        detailsPanel.add(priceLabel, BorderLayout.SOUTH);
        
        // Book now button
        JButton bookButton = new JButton("Book Now");
        bookButton.setBackground(new Color(0, 153, 0));
        bookButton.setForeground(Color.WHITE);
        bookButton.setPreferredSize(new Dimension(120, 40));
        bookButton.addActionListener(e -> handleBooking(room));
        
        panel.add(imagePanel, BorderLayout.WEST);
        panel.add(detailsPanel, BorderLayout.CENTER);
        panel.add(bookButton, BorderLayout.EAST);
        
        return panel;
    }
    
    private static void handleBooking(Room room) {
        if (currentUser == null) {
            int choice = JOptionPane.showConfirmDialog(
                mainFrame,
                "You need to be logged in to book a room. Would you like to login now?",
                "Login Required",
                JOptionPane.YES_NO_OPTION
            );
            
            if (choice == JOptionPane.YES_OPTION) {
                LoginDialog loginDialog = new LoginDialog(mainFrame);
                loginDialog.setVisible(true);
                
                if (loginDialog.isSucceeded()) {
                    currentUser = loginDialog.getLoggedInUser();
                    // Try booking again after successful login
                    handleBooking(room);
                }
            }
            return;
        }
        
        int result = JOptionPane.showConfirmDialog(
            mainFrame,
            String.format("Would you like to book %s for $%.2f per night?\n\nBooking Details:\nGuest: %s\nContact: %s\nEmail: %s",
                room.getName(),
                room.getPrice(),
                currentUser.getFullName(),
                currentUser.getPhoneNumber(),
                currentUser.getEmail()),
            "Confirm Booking",
            JOptionPane.YES_NO_OPTION
        );
        
        if (result == JOptionPane.YES_OPTION) {
            // TODO: Save booking to database
            JOptionPane.showMessageDialog(
                mainFrame,
                "Booking confirmed! A confirmation email will be sent to " + currentUser.getEmail(),
                "Booking Successful",
                JOptionPane.INFORMATION_MESSAGE
            );
        }
    }
}

class Room {
    private String name;
    private String view;
    private int capacity;
    private double price;
    private String description;
    
    public Room(String name, String view, int capacity, double price, String description) {
        this.name = name;
        this.view = view;
        this.capacity = capacity;
        this.price = price;
        this.description = description;
    }
    
    public String getName() { return name; }
    public String getView() { return view; }
    public int getCapacity() { return capacity; }
    public double getPrice() { return price; }
    public String getDescription() { return description; }
}
