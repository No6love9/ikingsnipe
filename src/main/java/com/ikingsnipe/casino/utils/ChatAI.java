package com.ikingsnipe.casino.utils;

import com.ikingsnipe.casino.managers.ProfitTracker;
import com.ikingsnipe.casino.models.CasinoConfig;
import org.dreambot.api.methods.input.Keyboard;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import java.util.*;

/**
 * Advanced Chat AI for Elite Titan Casino Pro
 * Features:
 * - Comprehensive command system (!rules, !stats, !top, !hash, !games)
 * - Automated player engagement
 * - Robust alias recognition
 * - Dynamic data injection (profit, winners, seeds)
 */
public class ChatAI {
    private final Map<String, String[]> responses = new HashMap<>();
    private final Map<String, String> commandMap = new HashMap<>();
    private final Random random = new Random();
    private ProfitTracker profitTracker;
    private CasinoConfig config;
    private ProvablyFair provablyFair;

    public ChatAI() {
        initializeResponses();
        initializeCommands();
    }

    public ChatAI(CasinoConfig config) {
        this.config = config;
        initializeResponses();
        initializeCommands();
    }

    private void initializeResponses() {
        responses.put("legit", new String[]{"100% legit, use !hash to verify", "Fair games only, check my history", "Trusted host, fast payouts!"});
        responses.put("how to play", new String[]{"Trade me and type !rules for help", "Just trade me to start, all games are listed in !games"});
        responses.put("scam", new String[]{"No scams here, check !hash for proof", "I'm a professional host, check my stats"});
        responses.put("luck", new String[]{"Good luck to everyone!", "May the RNG be in your favor", "Big wins incoming?"});
        responses.put("payout", new String[]{"Total payouts today: %s!", "Join the winners, %s paid out so far!"});
        responses.put("winner", new String[]{"Recent big winner: %s!", "Congrats to %s for the win!"});
    }

    private void initializeCommands() {
        // Game Aliases
        commandMap.put("!c", "craps");
        commandMap.put("!craps", "craps");
        commandMap.put("!dw", "dicewar");
        commandMap.put("!dice", "dice");
        commandMap.put("!bj", "blackjack");
        commandMap.put("!fp", "flower");
        commandMap.put("!55", "55x2");
        commandMap.put("!hc", "hotcold");
        
        // Utility Commands
        commandMap.put("!rules", "rules");
        commandMap.put("!stats", "stats");
        commandMap.put("!top", "top");
        commandMap.put("!hash", "hash");
        commandMap.put("!games", "games");
    }

    public void setProfitTracker(ProfitTracker tracker) { this.profitTracker = tracker; }
    public void setConfig(CasinoConfig config) { this.config = config; }
    public void setProvablyFair(ProvablyFair pf) { this.provablyFair = pf; }

    public void handleChat(String sender, String message) {
        handleChat(sender, message, false);
    }

    public void handleChat(String sender, String message, boolean isClan) {
        if (message == null || message.isEmpty()) return;
        if (isClan && !config.clanChatRespondToCommands) return;
        
        String lower = message.toLowerCase().trim();

        // 1. Handle Utility Commands
        if (lower.startsWith("!rules")) { handleRules(sender); return; }
        if (lower.startsWith("!stats")) { handleStats(sender); return; }
        if (lower.startsWith("!top")) { handleTop(sender); return; }
        if (lower.startsWith("!hash")) { handleHash(sender); return; }
        if (lower.startsWith("!games")) { handleGames(sender); return; }

        // 2. Handle Game Commands
        for (Map.Entry<String, String> entry : commandMap.entrySet()) {
            if (lower.startsWith(entry.getKey())) {
                handleGameCommand(sender, entry.getValue());
                return;
            }
        }

        // 3. Handle General Conversation
        for (String key : responses.keySet()) {
            if (lower.contains(key)) {
                sendResponse(key, sender);
                return;
            }
        }
    }

    private void handleRules(String sender) {
        typeMessage(sender + ": Trade me, offer your bet, and type the game command (e.g., !c). Min: " + formatGP(config.minBet));
    }

    private void handleStats(String sender) {
        if (profitTracker == null) return;
        typeMessage(String.format("%s: Total Wagered: %s | Total Payouts: %s", 
            sender, formatGP(profitTracker.getTotalWagered()), formatGP(profitTracker.getTotalPaidOut())));
    }

    private void handleTop(String sender) {
        if (profitTracker == null || profitTracker.getRecentWinners().isEmpty()) {
            typeMessage(sender + ": No big winners yet today. Be the first!");
            return;
        }
        typeMessage("Top Winner: " + profitTracker.getRecentWinners().get(0));
    }

    private void handleHash(String sender) {
        if (provablyFair == null) return;
        typeMessage(sender + ": Current Hash: " + provablyFair.getHash().substring(0, 10) + "...");
    }

    private void handleGames(String sender) {
        typeMessage("Available: !c (Craps), !dw (DiceWar), !bj (Blackjack), !fp (Flower), !55 (55x2), !hc (HotCold)");
    }

    private void handleGameCommand(String sender, String gameKey) {
        if (config == null) return;
        CasinoConfig.GameSettings settings = config.games.get(gameKey);
        if (settings != null && settings.enabled) {
            typeMessage(String.format("%s: %s is ACTIVE! Multiplier: x%.2f. Trade me to play!", 
                sender, settings.name, settings.multiplier));
        }
    }

    private void sendResponse(String key, String sender) {
        String[] options = responses.get(key);
        String reply = options[random.nextInt(options.length)];
        
        if (key.equals("payout") && profitTracker != null) {
            reply = String.format(reply, formatGP(profitTracker.getTotalPaidOut()));
        } else if (key.equals("winner") && profitTracker != null && !profitTracker.getRecentWinners().isEmpty()) {
            reply = String.format(reply, profitTracker.getRecentWinners().get(0));
        }

        typeMessage(sender + ": " + reply);
    }

    private void typeMessage(String message) {
        typeMessage(message, false);
    }

    private void typeMessage(String message, boolean isClan) {
        Logger.log("[ChatAI] " + (isClan ? "[Clan] " : "[Public] ") + message);
        Sleep.sleep(800, 1500);
        if (isClan) {
            Keyboard.type("/" + message, true);
        } else {
            Keyboard.type(message, true);
        }
    }

    private String formatGP(long a) {
        if (a >= 1_000_000) return (a / 1_000_000) + "M";
        if (a >= 1_000) return (a / 1_000) + "K";
        return String.valueOf(a);
    }

    /**
     * Send a message to chat
     */
    public void sendMessage(String message, boolean isClan) {
        typeMessage(message, isClan);
    }

    /**
     * Handle incoming message
     */
    public void handleMessage(String sender, String message) {
        handleChat(sender, message, false);
    }
}
