package com.ikingsnipe.core;

import org.dreambot.api.methods.input.Keyboard;


import java.security.MessageDigest;
import java.util.Base64;

/**
 * License Manager for GoatGang Edition
 * Handles license key generation and validation
 */
public class LicenseManager {
    private static final String SALT = "GoatGang_iKingSnipe_2026";

    /**
     * Generate a license key based on a username or machine ID
     */
    public static String generateKey(String input) {
        try {
            String combined = input + SALT;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(combined.getBytes("UTF-8"));
            String encoded = Base64.getEncoder().encodeToString(hash);
            return "GG-" + encoded.substring(0, 4) + "-" + encoded.substring(4, 8) + "-" + encoded.substring(8, 12);
        } catch (Exception e) {
            return "GG-ERROR-KEY-GEN";
        }
    }

    /**
     * Validate a license key
     */
    public static boolean validateKey(String input, String key) {
        return generateKey(input).equals(key);
    }
}
