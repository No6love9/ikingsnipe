package com.ikingsnipe.casino.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class ProvablyFair {
    private String serverSeed;
    private String clientSeed;
    private int nonce;

    public ProvablyFair() {
        this.serverSeed = generateRandomSeed();
        this.nonce = 0;
    }

    public void setClientSeed(String clientSeed) {
        this.clientSeed = clientSeed;
    }

    public String getServerSeedHash() {
        return hash(serverSeed);
    }

    public int generateRoll(int min, int max) {
        String combined = serverSeed + ":" + clientSeed + ":" + nonce;
        String hash = hash(combined);
        nonce++;
        
        // Use first 8 characters of hash to generate a number
        long value = Long.parseLong(hash.substring(0, 8), 16);
        return (int) (min + (value % (max - min + 1)));
    }

    private String generateRandomSeed() {
        byte[] array = new byte[32];
        new Random().nextBytes(array);
        return bytesToHex(array);
    }

    private String hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            return input;
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public String revealServerSeed() {
        String oldSeed = serverSeed;
        serverSeed = generateRandomSeed(); // Rotate seed
        nonce = 0;
        return oldSeed;
    }
}
