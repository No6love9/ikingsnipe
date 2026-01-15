package com.ikingsnipe.casino.utils;
import java.security.MessageDigest; import java.util.UUID;
public class ProvablyFair {
    private String serverSeed;
    public void newRound(String user) { serverSeed = UUID.randomUUID().toString(); }
    public String getServerSeedHash() {
        try {
            MessageDigest d = MessageDigest.getInstance("SHA-256");
            byte[] h = d.digest(serverSeed.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder(); for (byte b : h) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { return ""; }
    }
    public String revealServerSeed() { return serverSeed; }
}
