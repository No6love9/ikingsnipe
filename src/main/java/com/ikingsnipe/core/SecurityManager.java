package com.ikingsnipe.core;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

/**
 * Security Manager for iKingSnipe GoatGang Edition
 * Handles JAR locking and password protection
 */
public class SecurityManager {
    private static final String MASTER_PASSWORD = "sheba777";

    public static boolean authenticate() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(20, 20, 25));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel label = new JLabel("Enter Master Password to Unlock GoatGang Edition:");
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(label, BorderLayout.NORTH);

        JPasswordField passwordField = new JPasswordField(15);
        passwordField.setBackground(new Color(45, 45, 55));
        passwordField.setForeground(Color.WHITE);
        passwordField.setCaretColor(Color.WHITE);
        passwordField.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 120)));
        panel.add(passwordField, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(null, panel, "GoatGang Security", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            char[] input = passwordField.getPassword();
            boolean authorized = Arrays.equals(input, MASTER_PASSWORD.toCharArray());
            Arrays.fill(input, '0'); // Clear password from memory
            return authorized;
        }
        return false;
    }
}
