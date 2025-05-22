package main.java.com.hotel;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;

public class RegisterDialog extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField emailField;
    private JTextField fullNameField;
    private JTextField phoneField;
    private boolean succeeded;

    public RegisterDialog(Frame parent) {
        super(parent, "Register", true);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        cs.fill = GridBagConstraints.HORIZONTAL;
        cs.insets = new Insets(0, 0, 10, 10);

        // Username
        addFormField(panel, cs, "Username:", usernameField = new JTextField(20), 0);
        
        // Password
        addFormField(panel, cs, "Password:", passwordField = new JPasswordField(20), 1);
        
        // Confirm Password
        addFormField(panel, cs, "Confirm Password:", confirmPasswordField = new JPasswordField(20), 2);
        
        // Email
        addFormField(panel, cs, "Email:", emailField = new JTextField(20), 3);
        
        // Full Name
        addFormField(panel, cs, "Full Name:", fullNameField = new JTextField(20), 4);
        
        // Phone
        addFormField(panel, cs, "Phone:", phoneField = new JTextField(20), 5);

        // Buttons panel
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRegister = new JButton("Register");
        btnRegister.addActionListener(e -> register());
        
        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        bp.add(btnRegister);
        bp.add(btnCancel);

        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(bp, BorderLayout.PAGE_END);

        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    private void addFormField(JPanel panel, GridBagConstraints cs, String label, JComponent field, int gridy) {
        JLabel lbl = new JLabel(label);
        cs.gridx = 0;
        cs.gridy = gridy;
        cs.gridwidth = 1;
        panel.add(lbl, cs);

        cs.gridx = 1;
        cs.gridwidth = 2;
        panel.add(field, cs);
    }

    private void register() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String email = emailField.getText();
        String fullName = fullNameField.getText();
        String phone = phoneField.getText();

        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || fullName.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please fill all required fields",
                "Registration Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this,
                "Passwords do not match",
                "Registration Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create new user with hashed password
        String hashedPassword = DatabaseManager.hashPassword(password);
        User newUser = new User(username, email, hashedPassword, fullName, phone);

        if (DatabaseManager.registerUser(newUser)) {
            JOptionPane.showMessageDialog(this,
                "Registration successful! Please login.",
                "Registration Success",
                JOptionPane.INFORMATION_MESSAGE);
            succeeded = true;
            dispose();
            new LoginDialog(null).setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this,
                "Username or email already exists",
                "Registration Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSucceeded() {
        return succeeded;
    }
} 