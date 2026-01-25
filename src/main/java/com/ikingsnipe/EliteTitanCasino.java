package com.ikingsnipe;

import com.ikingsnipe.casino.games.impl.CrapsGame;
import com.ikingsnipe.casino.games.impl.FiftyFiveGame;
import com.ikingsnipe.casino.games.GameResult;
import com.ikingsnipe.casino.gui.CasinoGUI;
import com.ikingsnipe.casino.managers.HumanizationManager;
import com.ikingsnipe.casino.models.CasinoConfig;
import com.ikingsnipe.casino.utils.ProvablyFairCraps;

import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.trade.Trade;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.input.Keyboard;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.items.Item;

import java.awt.*;
import java.util.Arrays;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

/**
 * Elite Titan Casino Bot - Main Entry Point
 * 
 * A provably fair OSRS casino bot with advanced anti-ban features.
 * Compatible with DreamBot API 4.x (auto-updated)
 * 
 * Features:
 * - Provably Fair Craps & Dice (SHA-256 commitment)
 * - Velocity-varied Bezier mouse paths
 * - Full trade handling
 * - GUI configuration
 * - Anti-ban humanization
 * 
 * @author iKingSnipe / EliteForge
 * @version 8.2.8-guaranteed
 */
@ScriptManifest(
    name = "Elite Titan Casino",
    author = "iKingSnipe",
    version = 8.28,
    category = Category.MISC,
    description = "Provably Fair Casino Bot - Craps, Dice, Anti-Ban"
)
public class EliteTitanCasino extends AbstractScript {
    
    // ══════════════════════════════════════════════════════════════════════════
    // CONSTANTS
    // ══════════════════════════════════════════════════════════════════════════
    
    private static final int COINS_ID = 995;
    private static final int PLATINUM_TOKEN_ID = 13204;
    private static final long TOKEN_VALUE = 1000L;
    
    // ══════════════════════════════════════════════════════════════════════════
    // STATE
    // ══════════════════════════════════════════════════════════════════════════
    
    private enum State {
        IDLE,
        ADVERTISING,
        TRADING,
        PLAYING,
        PAYOUT,
        RECOVERY
    }
    
    private State currentState = State.IDLE;
    private boolean started = false;
    private boolean guiComplete = false;
    
    // ══════════════════════════════════════════════════════════════════════════
    // MANAGERS & GAMES
    // ══════════════════════════════════════════════════════════════════════════
    
    private CasinoConfig config;
    private CasinoGUI gui;
    private HumanizationManager humanization;
    private CrapsGame crapsGame;
    private FiftyFiveGame diceGame;
    private SecureRandom sr;
    
    // ══════════════════════════════════════════════════════════════════════════
    // SESSION TRACKING
    // ══════════════════════════════════════════════════════════════════════════
    
    private long sessionProfit = 0;
    private int sessionWins = 0;
    private int sessionLosses = 0;
    private long startTime = 0;
    
    // Player balances (in-memory for this session)
    private Map<String, Long> playerBalances = new HashMap<>();
    
    // Current trade state
    private String currentTrader = null;
    private long currentBet = 0;
    private String currentGame = "craps";
    
    // ══════════════════════════════════════════════════════════════════════════
    // SCRIPT LIFECYCLE
    // ══════════════════════════════════════════════════════════════════════════
    
    @Override
    public void onStart() {
        Logger.log("═══════════════════════════════════════════════════════════");
        Logger.log("  Elite Titan Casino v8.2.8 - Starting...");
        Logger.log("  Author: iKingSnipe | EliteForge");
        Logger.log("═══════════════════════════════════════════════════════════");
        
        // Initialize components
        sr = new SecureRandom();
        config = CasinoConfig.load();
        humanization = new HumanizationManager(config);
        crapsGame = new CrapsGame();
        diceGame = new FiftyFiveGame();
        startTime = System.currentTimeMillis();
        
        // Show GUI
        try {
            gui = new CasinoGUI(config, (start) -> {
                this.started = start;
                this.guiComplete = true;
                if (start) {
                    Logger.log("[EliteTitan] GUI closed - Script STARTED");
                    currentState = State.IDLE;
                } else {
                    Logger.log("[EliteTitan] GUI closed - Script CANCELLED");
                }
            });
            gui.setVisible(true);
        } catch (Exception e) {
            Logger.error("[EliteTitan] GUI Error: " + e.getMessage());
            // Continue without GUI
            started = true;
            guiComplete = true;
        }
        
        Logger.log("[EliteTitan] Initialization complete. Waiting for GUI...");
    }
    
    @Override
    public int onLoop() {
        // Wait for GUI
        if (!guiComplete) {
            return 500;
        }
        
        // Check if started
        if (!started) {
            Logger.log("[EliteTitan] Script not started. Stopping.");
            stop();
            return -1;
        }
        
        try {
            // Apply humanization
            humanization.applyIdleHumanization();
            
            // State machine
            switch (currentState) {
                case IDLE:
                    return handleIdle();
                    
                case ADVERTISING:
                    return handleAdvertising();
                    
                case TRADING:
                    return handleTrading();
                    
                case PLAYING:
                    return handlePlaying();
                    
                case PAYOUT:
                    return handlePayout();
                    
                case RECOVERY:
                    return handleRecovery();
                    
                default:
                    currentState = State.IDLE;
                    return 1000;
            }
            
        } catch (Exception e) {
            Logger.error("[EliteTitan] Error in loop: " + e.getMessage());
            currentState = State.RECOVERY;
            return 2000;
        }
    }
    
    // ══════════════════════════════════════════════════════════════════════════
    // STATE HANDLERS
    // ══════════════════════════════════════════════════════════════════════════
    
    private int handleIdle() {
        // Check for incoming trades
        if (Trade.isOpen()) {
            currentState = State.TRADING;
            return 100;
        }
        
        // Check for trade requests (players nearby)
        Player local = Players.getLocal();
        if (local != null) {
            // Periodically advertise
            if (Calculations.random(1, 100) <= 5) {
                currentState = State.ADVERTISING;
                return 100;
            }
        }
        
        // Check for breaks
        if (humanization.shouldTakeBreak()) {
            humanization.takeBreak();
        }
        
        return Calculations.random(800, 1500);
    }
    
    private int handleAdvertising() {
        try {
            String[] ads = {
                "Casino open! !c for Craps, !55 for Dice | Provably Fair",
                "Hosting games! Trade me | !c = Craps x3 | !55 = Dice x2",
                "Elite Casino | Provably Fair | !c !55 | Trade to play",
                "GoatGang Casino | SHA-256 verified | Trade me!"
            };
            
            String ad = ads[sr.nextInt(ads.length)];
            
            // Add random suffix for anti-mute
            if (config.randomChatSuffix && sr.nextBoolean()) {
                String[] suffixes = {" :)", " !", " ~", " .."};
                ad += suffixes[sr.nextInt(suffixes.length)];
            }
            
            Keyboard.type(ad, true);
            humanization.sleepGaussian(500, 1000);
            
        } catch (Exception e) {
            Logger.warn("[EliteTitan] Ad error: " + e.getMessage());
        }
        
        currentState = State.IDLE;
        return Calculations.random(25000, 45000); // Wait 25-45s before next ad
    }
    
    private int handleTrading() {
        if (!Trade.isOpen()) {
            currentState = State.IDLE;
            currentTrader = null;
            currentBet = 0;
            return 500;
        }
        
        try {
            // Get trader name
            if (currentTrader == null) {
                currentTrader = Trade.getTradingWith();
                if (currentTrader != null) {
                    Logger.log("[EliteTitan] Trade opened with: " + currentTrader);
                    
                    // Announce commitment hash
                    String hash = crapsGame.getPreRollCommitment();
                    Keyboard.type("/Hash: " + hash + " | !c=Craps !55=Dice", true);
                    humanization.sleepGaussian(300, 600);
                }
            }
            
            // Handle trade screen 1
            if (Trade.isOpen(1)) {
                return handleTradeScreen1();
            }
            
            // Handle trade screen 2
            if (Trade.isOpen(2)) {
                return handleTradeScreen2();
            }
            
        } catch (Exception e) {
            Logger.error("[EliteTitan] Trade error: " + e.getMessage());
            Trade.close();
            currentState = State.RECOVERY;
        }
        
        return 300;
    }
    
    private int handleTradeScreen1() {
        // Calculate their offer
        long theirCoins = Arrays.stream(Trade.getTheirItems()).filter(i -> i.getID() == COINS_ID).mapToLong(i -> i.getAmount()).sum();
        long theirTokens = Arrays.stream(Trade.getTheirItems()).filter(i -> i.getID() == PLATINUM_TOKEN_ID).mapToLong(i -> i.getAmount()).sum();
        long totalOffer = theirCoins + (theirTokens * TOKEN_VALUE);
        
        if (totalOffer > 0) {
            currentBet = totalOffer;
            Logger.log("[EliteTitan] Bet received: " + formatGP(currentBet));
            
            // Accept trade
            humanization.sleepGaussian(400, 800);
            Trade.acceptTrade();
        }
        
        return 500;
    }
    
    private int handleTradeScreen2() {
        // Verify amounts haven't changed
        long theirCoins = Arrays.stream(Trade.getTheirItems()).filter(i -> i.getID() == COINS_ID).mapToLong(i -> i.getAmount()).sum();
        long theirTokens = Arrays.stream(Trade.getTheirItems()).filter(i -> i.getID() == PLATINUM_TOKEN_ID).mapToLong(i -> i.getAmount()).sum();
        long totalOffer = theirCoins + (theirTokens * TOKEN_VALUE);
        
        if (totalOffer != currentBet) {
            // Amount changed - SCAM ATTEMPT
            Logger.warn("[EliteTitan] SCAM DETECTED! Amount changed from " + currentBet + " to " + totalOffer);
            Keyboard.type("/SCAM ALERT: Amount changed! Trade declined.", true);
            Trade.close();
            currentState = State.RECOVERY;
            return 1000;
        }
        
        // Accept final trade
        humanization.sleepGaussian(300, 600);
        Trade.acceptTrade();
        
        // Wait for trade to complete
        Sleep.sleepUntil(() -> !Trade.isOpen(), 5000);
        
        if (!Trade.isOpen()) {
            // Trade completed - play game
            currentState = State.PLAYING;
        }
        
        return 500;
    }
    
    private int handlePlaying() {
        if (currentTrader == null || currentBet <= 0) {
            currentState = State.IDLE;
            return 500;
        }
        
        try {
            Logger.log("[EliteTitan] Playing " + currentGame + " for " + currentTrader + " - Bet: " + formatGP(currentBet));
            
            // Announce pre-roll
            Keyboard.type("/Rolling for " + currentTrader + "... Hash: " + crapsGame.getPreRollCommitment(), true);
            humanization.sleepGaussian(1000, 2000);
            
            // Play the game
            GameResult result;
            if (currentGame.equals("55") || currentGame.equals("dice")) {
                result = diceGame.play(currentTrader, currentBet, null);
            } else {
                result = crapsGame.play(currentTrader, currentBet, null);
            }
            
            // Announce result
            Keyboard.type("/" + result.getDescription(), true);
            humanization.sleepGaussian(500, 1000);
            
            // Update stats
            if (result.isWin()) {
                sessionWins++;
                sessionProfit -= result.getPayout();
                
                // Store payout amount for payout phase
                playerBalances.put(currentTrader, result.getPayout());
                currentState = State.PAYOUT;
                
                Keyboard.type("/Winner! Payout: " + formatGP(result.getPayout()) + " | Seed: " + crapsGame.getRevealString(), true);
            } else {
                sessionLosses++;
                sessionProfit += currentBet;
                
                Keyboard.type("/Better luck next time! | Seed: " + crapsGame.getRevealString(), true);
                currentState = State.IDLE;
            }
            
            // Post-action anti-ban
            humanization.postActionAntiBan();
            
        } catch (Exception e) {
            Logger.error("[EliteTitan] Game error: " + e.getMessage());
            currentState = State.RECOVERY;
        }
        
        // Reset trade state
        currentTrader = null;
        currentBet = 0;
        
        return 1000;
    }
    
    private int handlePayout() {
        // Find player to pay
        String playerToPay = null;
        long amountToPay = 0;
        
        for (Map.Entry<String, Long> entry : playerBalances.entrySet()) {
            if (entry.getValue() > 0) {
                playerToPay = entry.getKey();
                amountToPay = entry.getValue();
                break;
            }
        }
        
        if (playerToPay == null) {
            currentState = State.IDLE;
            return 500;
        }
        
        try {
            // Check if we have enough coins
            long ourCoins = Inventory.count(COINS_ID);
            long ourTokens = Inventory.count(PLATINUM_TOKEN_ID);
            long ourTotal = ourCoins + (ourTokens * TOKEN_VALUE);
            
            if (ourTotal < amountToPay) {
                Logger.warn("[EliteTitan] Insufficient funds for payout! Need: " + formatGP(amountToPay) + ", Have: " + formatGP(ourTotal));
                Keyboard.type("/Payout delayed - admin will process manually. Amount: " + formatGP(amountToPay), true);
                playerBalances.remove(playerToPay);
                currentState = State.IDLE;
                return 2000;
            }
            
            // Find player and trade
            Player target = Players.closest(playerToPay);
            if (target != null && target.exists()) {
                target.interact("Trade with");
                Sleep.sleepUntil(Trade::isOpen, 5000);
                
                if (Trade.isOpen()) {
                    // Offer coins using inventory interaction
                    // DreamBot Trade API uses offer() method
                    if (amountToPay <= ourCoins) {
                        // Right-click coins -> Offer-X -> Enter amount
                        if (Inventory.contains(COINS_ID)) {
                            Inventory.interact(COINS_ID, "Offer-X");
                            Sleep.sleep(600, 1000);
                            Keyboard.type(String.valueOf(amountToPay), true);
                        }
                    } else {
                        // Need to use tokens - offer all coins first
                        if (Inventory.contains(COINS_ID)) {
                            Inventory.interact(COINS_ID, "Offer-All");
                            Sleep.sleep(400, 800);
                        }
                        // Then offer tokens
                        long tokensNeeded = (amountToPay - ourCoins) / TOKEN_VALUE + 1;
                        if (Inventory.contains(PLATINUM_TOKEN_ID)) {
                            Inventory.interact(PLATINUM_TOKEN_ID, "Offer-X");
                            Sleep.sleep(600, 1000);
                            Keyboard.type(String.valueOf(tokensNeeded), true);
                        }
                    }
                    
                    humanization.sleepGaussian(500, 1000);
                    Trade.acceptTrade();
                    
                    Sleep.sleepUntil(() -> Trade.isOpen(2), 5000);
                    if (Trade.isOpen(2)) {
                        Trade.acceptTrade();
                        Sleep.sleepUntil(() -> !Trade.isOpen(), 5000);
                    }
                    
                    if (!Trade.isOpen()) {
                        Logger.log("[EliteTitan] Payout complete: " + formatGP(amountToPay) + " to " + playerToPay);
                        playerBalances.remove(playerToPay);
                    }
                }
            } else {
                Logger.warn("[EliteTitan] Player " + playerToPay + " not found for payout");
                Keyboard.type("/" + playerToPay + " - Trade me for your payout: " + formatGP(amountToPay), true);
            }
            
        } catch (Exception e) {
            Logger.error("[EliteTitan] Payout error: " + e.getMessage());
        }
        
        currentState = State.IDLE;
        return 2000;
    }
    
    private int handleRecovery() {
        Logger.log("[EliteTitan] Recovery mode - resetting state");
        
        // Close any open interfaces
        if (Trade.isOpen()) {
            Trade.close();
        }
        if (Dialogues.inDialogue()) {
            Dialogues.clickContinue();
        }
        
        // Reset state
        currentTrader = null;
        currentBet = 0;
        
        humanization.sleepGaussian(2000, 4000);
        
        currentState = State.IDLE;
        return 2000;
    }
    
    // ══════════════════════════════════════════════════════════════════════════
    // PAINT OVERLAY
    // ══════════════════════════════════════════════════════════════════════════
    
    @Override
    public void onPaint(Graphics g) {
        if (!started) return;
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Background
        g2d.setColor(new Color(17, 17, 17, 220));
        g2d.fillRoundRect(5, 5, 250, 140, 10, 10);
        
        // Border
        g2d.setColor(new Color(74, 144, 226));
        g2d.drawRoundRect(5, 5, 250, 140, 10, 10);
        
        // Title
        g2d.setColor(new Color(212, 175, 55));
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("Elite Titan Casino v8.2.8", 15, 25);
        
        // Stats
        g2d.setColor(new Color(224, 224, 224));
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        
        g2d.drawString("State: " + currentState.name(), 15, 45);
        g2d.drawString("Wins: " + sessionWins + " | Losses: " + sessionLosses, 15, 65);
        
        // Profit color
        if (sessionProfit >= 0) {
            g2d.setColor(new Color(76, 175, 80));
        } else {
            g2d.setColor(new Color(244, 67, 54));
        }
        g2d.drawString("Profit: " + formatGP(sessionProfit), 15, 85);
        
        // RNG Status
        g2d.setColor(new Color(224, 224, 224));
        g2d.drawString("Hash: " + crapsGame.getPreRollCommitment() + "...", 15, 105);
        g2d.drawString("Nonce: " + crapsGame.getCurrentNonce() + " | Cycle: " + crapsGame.getRollsSinceRotation() + "/50", 15, 125);
        
        // Runtime
        long runtime = System.currentTimeMillis() - startTime;
        g2d.drawString("Runtime: " + formatTime(runtime), 15, 140);
    }
    
    // ══════════════════════════════════════════════════════════════════════════
    // UTILITIES
    // ══════════════════════════════════════════════════════════════════════════
    
    private String formatGP(long amount) {
        if (Math.abs(amount) >= 1_000_000_000) {
            return String.format("%.2fB", amount / 1_000_000_000.0);
        } else if (Math.abs(amount) >= 1_000_000) {
            return String.format("%.2fM", amount / 1_000_000.0);
        } else if (Math.abs(amount) >= 1_000) {
            return String.format("%.1fK", amount / 1_000.0);
        }
        return String.valueOf(amount);
    }
    
    private String formatTime(long ms) {
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        return String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60);
    }
    
    @Override
    public void onExit() {
        Logger.log("═══════════════════════════════════════════════════════════");
        Logger.log("  Elite Titan Casino - Shutting down");
        Logger.log("  Session Stats: W:" + sessionWins + " L:" + sessionLosses + " Profit:" + formatGP(sessionProfit));
        Logger.log("═══════════════════════════════════════════════════════════");
        
        if (gui != null) {
            gui.dispose();
        }
    }
}
