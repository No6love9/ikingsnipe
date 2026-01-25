package com.ikingsnipe.casino.utils;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import org.dreambot.api.utilities.Logger;

/**
 * ProvablyFairCraps - Elite Titan Casino Bot
 * 
 * Features:
 * - SecureRandom per-session seed + nonce chain
 * - SHA-256 pre-commit hash announced in chat BEFORE roll
 * - Full seed/nonce reveal AFTER outcome
 * - Auto-rotate seed every ~50 rolls (announce new commitment, reveal previous on rotation)
 * - Verification method exposed
 * - Nonce increment per roll
 * 
 * @author EliteForge / iKingSnipe
 * @version 8.2.7-velocity-bezier
 */
public class ProvablyFairCraps {
    
    // Cryptographic components
    private final SecureRandom secureRandom;
    private String currentSeed;
    private String previousSeed;
    private long nonce;
    private int rollsSinceSeedRotation;
    
    // Configuration
    private static final int SEED_ROTATION_THRESHOLD = 50;
    private static final int SEED_LENGTH = 32; // 256 bits
    
    // State tracking
    private String lastCommitHash;
    private String lastRollSeed;
    private long lastRollNonce;
    private int lastRollResult;
    
    public ProvablyFairCraps() {
        this.secureRandom = new SecureRandom();
        this.nonce = 0;
        this.rollsSinceSeedRotation = 0;
        rotateSeed(true); // Initial seed generation
    }
    
    /**
     * Generate a new random seed using SecureRandom
     */
    private String generateSecureSeed() {
        byte[] seedBytes = new byte[SEED_LENGTH];
        secureRandom.nextBytes(seedBytes);
        return bytesToHex(seedBytes);
    }
    
    /**
     * Rotate the seed - store previous for reveal, generate new
     * @param isInitial true if this is the initial seed generation
     */
    public void rotateSeed(boolean isInitial) {
        previousSeed = currentSeed;
        currentSeed = generateSecureSeed();
        nonce = 0;
        rollsSinceSeedRotation = 0;
        
        if (!isInitial) {
            Logger.log("[ProvablyFair] Seed rotated. Previous seed will be revealed.");
        }
        Logger.log("[ProvablyFair] New commitment hash: " + getCommitmentHash().substring(0, 16) + "...");
    }
    
    /**
     * Get the SHA-256 commitment hash of the current seed
     * This is announced BEFORE the roll
     */
    public String getCommitmentHash() {
        return sha256(currentSeed);
    }
    
    /**
     * Get a shortened version of the commitment hash for display
     */
    public String getShortCommitmentHash() {
        String hash = getCommitmentHash();
        return hash.substring(0, 16);
    }
    
    /**
     * Generate a provably fair dice roll (1-12 for craps, sum of 2d6)
     * @return Array of [die1, die2, total]
     */
    public int[] generateRoll() {
        // Check if we need to rotate seed
        if (rollsSinceSeedRotation >= SEED_ROTATION_THRESHOLD) {
            rotateSeed(false);
        }
        
        // Store current state for verification
        lastRollSeed = currentSeed;
        lastRollNonce = nonce;
        lastCommitHash = getCommitmentHash();
        
        // Generate the roll using seed + nonce
        String rollInput = currentSeed + ":" + nonce;
        String rollHash = sha256(rollInput);
        
        // Extract two dice values from the hash
        // Use first 8 chars for die1, next 8 for die2
        int die1 = (hexToInt(rollHash.substring(0, 8)) % 6) + 1;
        int die2 = (hexToInt(rollHash.substring(8, 16)) % 6) + 1;
        int total = die1 + die2;
        
        lastRollResult = total;
        
        // Increment nonce for next roll
        nonce++;
        rollsSinceSeedRotation++;
        
        Logger.log("[ProvablyFair] Roll generated: " + die1 + " + " + die2 + " = " + total + " (nonce: " + lastRollNonce + ")");
        
        return new int[]{die1, die2, total};
    }
    
    /**
     * Generate a provably fair dice roll for 55x (1-100)
     * @return Roll result 1-100
     */
    public int generateDiceRoll() {
        // Check if we need to rotate seed
        if (rollsSinceSeedRotation >= SEED_ROTATION_THRESHOLD) {
            rotateSeed(false);
        }
        
        // Store current state for verification
        lastRollSeed = currentSeed;
        lastRollNonce = nonce;
        lastCommitHash = getCommitmentHash();
        
        // Generate the roll using seed + nonce
        String rollInput = currentSeed + ":" + nonce;
        String rollHash = sha256(rollInput);
        
        // Extract roll value (1-100) from hash
        int roll = (hexToInt(rollHash.substring(0, 8)) % 100) + 1;
        
        lastRollResult = roll;
        
        // Increment nonce for next roll
        nonce++;
        rollsSinceSeedRotation++;
        
        Logger.log("[ProvablyFair] Dice roll generated: " + roll + " (nonce: " + lastRollNonce + ")");
        
        return roll;
    }
    
    /**
     * Get the reveal string for the last roll (seed:nonce)
     * This is announced AFTER the outcome
     */
    public String getRevealString() {
        return lastRollSeed + ":" + lastRollNonce;
    }
    
    /**
     * Get a shortened reveal string for chat
     */
    public String getShortRevealString() {
        return lastRollSeed.substring(0, 8) + "...:" + lastRollNonce;
    }
    
    /**
     * Get the previous seed for reveal on rotation
     */
    public String getPreviousSeed() {
        return previousSeed;
    }
    
    /**
     * Verify a roll given seed, nonce, and expected result
     * This method can be used by players to verify fairness
     */
    public static boolean verifyRoll(String seed, long nonce, int expectedTotal) {
        String rollInput = seed + ":" + nonce;
        String rollHash = sha256(rollInput);
        
        int die1 = (hexToInt(rollHash.substring(0, 8)) % 6) + 1;
        int die2 = (hexToInt(rollHash.substring(8, 16)) % 6) + 1;
        int total = die1 + die2;
        
        return total == expectedTotal;
    }
    
    /**
     * Verify a dice roll (1-100) given seed, nonce, and expected result
     */
    public static boolean verifyDiceRoll(String seed, long nonce, int expectedRoll) {
        String rollInput = seed + ":" + nonce;
        String rollHash = sha256(rollInput);
        
        int roll = (hexToInt(rollHash.substring(0, 8)) % 100) + 1;
        
        return roll == expectedRoll;
    }
    
    /**
     * Verify that a commitment hash matches a seed
     */
    public static boolean verifyCommitment(String commitmentHash, String seed) {
        return sha256(seed).equalsIgnoreCase(commitmentHash);
    }
    
    // ══════════════════════════════════════════════════════════════════════════
    // UTILITY METHODS
    // ══════════════════════════════════════════════════════════════════════════
    
    /**
     * SHA-256 hash function
     */
    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            Logger.error("[ProvablyFair] SHA-256 error: " + e.getMessage());
            return "ERROR";
        }
    }
    
    /**
     * Convert bytes to hex string
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    
    /**
     * Convert hex string to integer (handles overflow by using modulo)
     */
    private static int hexToInt(String hex) {
        // Use only first 7 chars to avoid overflow
        return Integer.parseUnsignedInt(hex.substring(0, 7), 16);
    }
    
    // ══════════════════════════════════════════════════════════════════════════
    // GETTERS FOR STATE TRACKING
    // ══════════════════════════════════════════════════════════════════════════
    
    public long getCurrentNonce() {
        return nonce;
    }
    
    public int getRollsSinceRotation() {
        return rollsSinceSeedRotation;
    }
    
    public String getLastCommitHash() {
        return lastCommitHash;
    }
    
    public int getLastRollResult() {
        return lastRollResult;
    }
    
    public boolean shouldRotateSoon() {
        return rollsSinceSeedRotation >= (SEED_ROTATION_THRESHOLD - 5);
    }
}
