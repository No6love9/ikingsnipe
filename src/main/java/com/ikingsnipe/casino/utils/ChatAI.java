package com.ikingsnipe.casino.utils;

import com.ikingsnipe.casino.managers.ProfitTracker;
import com.ikingsnipe.casino.models.CasinoConfig;
import org.dreambot.api.input.Keyboard;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * Enhanced Chat AI for snipes♧scripts Enterprise
 * Handles public chat commands and general conversation with robust recognition
 */
public class ChatAI {
    private final Map<String, String[]> responses = new HashMap<>();
    private final Map<String, String> commandMap = new HashMap<>();
    private final Random random = new Random();
    private ProfitTracker profitTracker;
    private CasinoConfig config;

    public ChatAI() {
        // General conversation responses
        responses.put("legit", new String[]{"Always legit, check my history!", "100% fair, use !hash to verify", "Trusted host here."});
        responses.put("how to play", new String[]{"Trade me and type !c for Craps or !dw for Dice War.", "Just trade me to start, commands are in the welcome message."});
        responses.put("scam", new String[]{"No scams here, provably fair only.", "Check the hash, I'm legit."});
        responses.put("luck", new String[]{"Good luck!", "May the RNG be with you.", "Big wins coming?"});
        responses.put("payout", new String[]{"I've paid out over %s today!", "Total payouts so far: %s. Join the winners!"});
        responses.put("winner", new String[]{"Last winner was %s!", "Check the overlay for recent winners like %s."});
        
        // Command aliases for robust recognition
        commandMap.put("!c", "craps");
        commandMap.put("!craps", "craps");
        commandMap.put("!dw", "dicewar");
        commandMap.put("!dicewar", "dicewar");
        commandMap.put("!dice", "dice");
        commandMap.put("!d", "dice");
        commandMap.put("!bj", "blackjack");
        commandMap.put("!blackjack", "blackjack");
        commandMap.put("!hc", "hotcold");
        commandMap.put("!hotcold", "hotcold");
        commandMap.put("!55", "55x2");
        commandMap.put("!fp", "flower");
        commandMap.put("!flower", "flower");
    }

    public void setProfitTracker(ProfitTracker tracker) {
        this.profitTracker = tracker;
    }

    public void setConfig(CasinoConfig config) {
        this.config = config;
    }

    /**
     * Handle incoming chat messages with robust command detection
     */
    public void handleChat(String sender, String message) {
        if (message == null || message.isEmpty()) return;
        String lower = message.toLowerCase().trim();

        // 1. Check for Game Commands (e.g., !c, !dw)
        for (Map.Entry<String, String> entry : commandMap.entrySet()) {
            if (lower.startsWith(entry.getKey())) {
                handleGameCommand(sender, entry.getValue());
                return;
            }
        }

        // 2. Check for General Conversation
        for (String key : responses.keySet()) {
            if (lower.contains(key)) {
                sendResponse(key, sender);
                return;
            }
        }
    }

    private void handleGameCommand(String sender, String gameKey) {
        if (config == null) return;
        
        CasinoConfig.GameSettings settings = config.games.get(gameKey);
        if (settings != null && settings.enabled) {
            String reply = String.format("%s: %s is ACTIVE! Multiplier: x%.2f. Trade me to play!", 
                sender, settings.name, settings.multiplier);
            typeMessage(reply);
        } else {
            typeMessage(sender + ": That game is currently disabled or unavailable.");
        }
    }

    private void sendResponse(String key, String sender) {
        String[] options = responses.get(key);
        String reply = options[random.nextInt(options.length)];
        
        // Dynamic Data Injection
        if (key.equals("payout") && profitTracker != null) {
            reply = String.format(reply, formatGP(Math.abs(profitTracker.getNetProfit())));
        } else if (key.equals("winner") && profitTracker != null && !profitTracker.getRecentWinners().isEmpty()) {
            reply = String.format(reply, profitTracker.getRecentWinners().get(0));
        }

        // Add sender name occasionally for humanization
        if (random.nextBoolean()) {
            reply = sender + ": " + reply;
        }

        typeMessage(reply);
    }

    private void typeMessage(String message) {
        Logger.log("[ChatAI] Replying: " + message);
        Sleep.sleep(1200, 2500); // Human-like reaction delay
        Keyboard.type(message, true);
    }

    private String formatGP(long a) {
        if (a >= 1_000_000) return (a / 1_000_000) + "M";
        if (a >= 1_000) return (a / 1_000) + "K";
        return String.valueOf(a);
    }
}
