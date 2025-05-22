package main.java.com.hotel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginDialog extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private boolean succeeded;
    private User loggedInUser;

    public LoginDialog(Frame parent) {
        super(parent, "Login", true);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        cs.fill = GridBagConstraints.HORIZONTAL;
        cs.insets = new Insets(0, 0, 10, 10);

        // Username
        addFormField(panel, cs, "Username:", usernameField = new JTextField(20), 0);
        
        // Password
        addFormField(panel, cs, "Password:", passwordField = new JPasswordField(20), 1);

        // Buttons panel
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnLogin = new JButton("Login");
        btnLogin.addActionListener(e -> login());
        
        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        bp.add(btnLogin);
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

    private void login() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter both username and password",
                "Login Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        loggedInUser = DatabaseManager.authenticateUser(username, password);
        if (loggedInUser != null) {
            succeeded = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                "Invalid username or password",
                "Login Error",
                JOptionPane.ERROR_MESSAGE);
            // Reset password field
            passwordField.setText("");
        }
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }
} 