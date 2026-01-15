package com.ikingsnipe.casino.utils;

import java.security.MessageDigest;
import java.util.UUID;

public class ProvablyFair {
    private String serverSeed;
    public void generateNewSeed() { serverSeed = UUID.randomUUID().toString(); }
    public String getHash() {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(serverSeed.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { return "ERROR"; }
    }
    public String getSeed() { return serverSeed; }
}
