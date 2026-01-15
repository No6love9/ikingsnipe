package com.ikingsnipe.casino.managers;

import com.ikingsnipe.casino.CasinoController;
import com.ikingsnipe.casino.models.PlayerSession;
import com.ikingsnipe.casino.utils.DreamBotAdapter;
import org.dreambot.api.methods.chat.Chat;
import org.dreambot.api.wrappers.widgets.message.Message;
import java.util.*;
import java.util.regex.*;

/**
 * Handles chat command parsing and routing
 * Processes !bet, !rules, !stats commands
 */
public class ChatManager {
    
    private final CasinoController controller;
    private final SessionManager sessionManager;
    private final TradeManager tradeManager;
    private final GameManager gameManager;
    private final DreamBotAdapter apiAdapter;
    
    // Command patterns
    private static final Pattern BET_PATTERN = Pattern.compile(
        "!bet\\s+(\\d+[kKmM]?)\\s+(craps|dice|flower)", 
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern RULES_PATTERN = Pattern.compile(
        "!rules\\s*(craps|dice|flower)?", 
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern STATS_PATTERN = Pattern.compile(
        "!stats", 
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern HELP_PATTERN = Pattern.compile(
        "!help", 
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern BALANCE_PATTERN = Pattern.compile(
        "!balance", 
        Pattern.CASE_INSENSITIVE
    );
    
    // Chat cooldown tracking
    private final Map<String, Long> playerCooldowns = new HashMap<>();
    private final Set<String> processedMessages = Collections.synchronizedSet(new HashSet<>());
    
    public ChatManager(CasinoController controller, SessionManager sessionManager,
                      TradeManager tradeManager, GameManager gameManager, DreamBotAdapter apiAdapter) {
        this.controller = controller;
        this.sessionManager = sessionManager;
        this.tradeManager = tradeManager;
        this.gameManager = gameManager;
        this.apiAdapter = apiAdapter;
        
        // Initialize chat listener
        Chat.addMessageListener(this::onMessage);
    }
    
    /**
     * Process incoming chat messages
     */
    public void processMessages() {
        // This is called periodically to process any pending messages
        // DreamBot's chat listener calls onMessage directly
    }
    
    /**
     * DreamBot chat listener callback
     */
    private void onMessage(Message message) {
        if (!controller.isRunning()) {
            return;
        }
        
        String msg = message.getMessage();
        String sender = message.getUsername();
        
        // Ignore messages from ourselves or system
        if (sender == null || sender.equals(apiAdapter.getPlayerName()) || 
            message.isAutomated() || processedMessages.contains(msg)) {
            return;
        }
        
        processedMessages.add(msg);
        
        // Process command
        try {
            handleCommand(sender, msg);
        } catch (Exception e) {
            controller.logError("Chat processing error from " + sender + ": " + e.getMessage());
        }
        
        // Clean up old processed messages
        if (processedMessages.size() > 1000) {
            processedMessages.clear();
        }
    }
    
    /**
     * Handle a chat command
     */
    private void handleCommand(String sender, String message) {
        // Check cooldown
        if (isOnCooldown(sender)) {
            return;
        }
        
        // Check for bet command
        Matcher betMatcher = BET_PATTERN.matcher(message);
        if (betMatcher.matches()) {
            handleBetCommand(sender, betMatcher.group(1), betMatcher.group(2));
            setCooldown(sender);
            return;
        }
        
        // Check for rules command
        Matcher rulesMatcher = RULES_PATTERN.matcher(message);
        if (rulesMatcher.matches()) {
            handleRulesCommand(sender, rulesMatcher.group(1));
            setCooldown(sender);
            return;
        }
        
        // Check for stats command
        Matcher statsMatcher = STATS_PATTERN.matcher(message);
        if (statsMatcher.matches()) {
            handleStatsCommand(sender);
            setCooldown(sender);
            return;
        }
        
        // Check for help command
        Matcher helpMatcher = HELP_PATTERN.matcher(message);
        if (helpMatcher.matches()) {
            handleHelpCommand(sender);
            setCooldown(sender);
            return;
        }
        
        // Check for balance command
        Matcher balanceMatcher = BALANCE_PATTERN.matcher(message);
        if (balanceMatcher.matches()) {
            handleBalanceCommand(sender);
            setCooldown(sender);
            return;
        }
    }
    
    /**
     * Handle !bet <amount> <game> command
     */
    private void handleBetCommand(String player, String betStr, String gameType) {
        controller.log("Bet command from " + player + ": " + betStr + " " + gameType);
        
        // Check if player already has active session
        if (sessionManager.hasActiveSession(player)) {
            sendMessage(player, "You already have an active bet. Please complete it first.");
            return;
        }
        
        // Check if player has active trade
        if (tradeManager.hasActiveTrade(player)) {
            sendMessage(player, "You already have an active trade in progress.");
            return;
        }
        
        // Parse bet amount
        int betAmount = parseBetAmount(betStr);
        if (betAmount <= 0) {
            sendMessage(player, "Invalid bet amount: " + betStr);
            return;
        }
        
        // Validate bet limits
        if (!controller.getConfig().isValidBet(betAmount)) {
            int min = controller.getConfig().getMinBet();
            int max = controller.getConfig().getMaxBet();
            sendMessage(player, "Bet must be between " + formatCoins(min) + " and " + formatCoins(max) + " GP.");
            return;
        }
        
        // Check if game is enabled
        if (!gameManager.isGameEnabled(gameType)) {
            sendMessage(player, gameType + " is currently disabled. Available games: " + 
                String.join(", ", gameManager.getEnabledGames()));
            return;
        }
        
        // Check session limit
        if (sessionManager.getActiveSessionCount() >= controller.getConfig().getMaxActiveSessions()) {
            sendMessage(player, "Casino is at capacity. Please try again in a moment.");
            return;
        }
        
        // Create player session
        PlayerSession session = sessionManager.createSession(player, gameType, betAmount);
        if (session == null) {
            sendMessage(player, "Unable to create betting session. Please try again.");
            return;
        }
        
        // Send confirmation
        String formattedBet = formatCoins(betAmount);
        sendMessage(player, "✅ Bet accepted: " + formattedBet + " GP on " + gameType + ". Opening trade...");
        
        controller.log("Created session for " + player + ": " + formattedBet + " GP on " + gameType);
        
        // Open trade with player
        if (apiAdapter.openTrade(player)) {
            controller.log("Trade opened with " + player);
        } else {
            sendMessage(player, "Failed to open trade. Please make sure you're nearby and try !bet again.");
            sessionManager.removeSession(player);
        }
    }
    
    /**
     * Handle !rules [game] command
     */
    private void handleRulesCommand(String player, String gameType) {
        if (gameType == null) {
            // Show all game rules
            StringBuilder response = new StringBuilder();
            response.append("🎰 Elite Titan Casino - Available Games:\n");
            response.append("Use !rules <game> for detailed rules\n\n");
            
            for (String game : gameManager.getEnabledGames()) {
                String rules = gameManager.getGameRules(game);
                response.append(game.toUpperCase()).append(": ")
                       .append(rules.substring(0, Math.min(50, rules.length())))
                       .append("...\n");
            }
            
            response.append("\nBetting: !bet <amount> <game>\n");
            response.append("Example: !bet 100k craps");
            
            sendMessage(player, response.toString());
        } else {
            // Show specific game rules
            String rules = gameManager.getGameRules(gameType);
            if (rules != null) {
                sendMessage(player, rules);
            } else {
                sendMessage(player, "Unknown game: " + gameType + ". Available: craps, dice, flower");
            }
        }
    }
    
    /**
     * Handle !stats command
     */
    private void handleStatsCommand(String player) {
        PlayerSession session = sessionManager.getSession(player);
        
        StringBuilder stats = new StringBuilder();
        stats.append("📊 Your Casino Statistics:\n");
        
        if (session != null) {
            stats.append("Active Bet: ").append(formatCoins(session.getBetAmount()))
                 .append(" GP on ").append(session.getGameType()).append("\n");
        }
        
        Map<String, Object> playerStats = sessionManager.getPlayerStats(player);
        if (playerStats != null && !playerStats.isEmpty()) {
            stats.append("Games Played: ").append(playerStats.getOrDefault("gamesPlayed", 0)).append("\n");
            stats.append("Wins: ").append(playerStats.getOrDefault("wins", 0)).append("\n");
            stats.append("Losses: ").append(playerStats.getOrDefault("losses", 0)).append("\n");
            
            int profit = (int) playerStats.getOrDefault("profit", 0);
            stats.append("Profit: ").append(profit >= 0 ? "+" : "").append(formatCoins(profit)).append(" GP\n");
        } else {
            stats.append("No games played yet. Place your first bet with !bet");
        }
        
        sendMessage(player, stats.toString());
    }
    
    /**
     * Handle !help command
     */
    private void handleHelpCommand(String player) {
        StringBuilder help = new StringBuilder();
        help.append("🎰 Elite Titan Casino - Commands:\n");
        help.append("!bet <amount> <game> - Place a bet (e.g., !bet 100k craps)\n");
        help.append("!rules [game] - Show game rules\n");
        help.append("!stats - View your statistics\n");
        help.append("!balance - Check your coin balance\n");
        help.append("!help - Show this message\n\n");
        help.append("Available Games: ").append(String.join(", ", gameManager.getEnabledGames())).append("\n");
        help.append("Min Bet: ").append(formatCoins(controller.getConfig().getMinBet())).append(" GP\n");
        help.append("Max Bet: ").append(formatCoins(controller.getConfig().getMaxBet())).append(" GP");
        
        sendMessage(player, help.toString());
    }
    
    /**
     * Handle !balance command
     */
    private void handleBalanceCommand(String player) {
        int balance = apiAdapter.getItemCount(995); // Coin ID
        sendMessage(player, "💰 Your balance: " + formatCoins(balance) + " GP");
    }
    
    // === UTILITY METHODS ===
    
    private int parseBetAmount(String betStr) {
        try {
            betStr = betStr.toUpperCase();
            
            if (betStr.endsWith("K")) {
                String number = betStr.substring(0, betStr.length() - 1);
                return (int) (Double.parseDouble(number) * 1000);
            } else if (betStr.endsWith("M")) {
                String number = betStr.substring(0, betStr.length() - 1);
                return (int) (Double.parseDouble(number) * 1000000);
            } else {
                return Integer.parseInt(betStr.replace(",", ""));
            }
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    private String formatCoins(int amount) {
        if (amount >= 1000000) {
            return String.format("%.1fM", amount / 1000000.0);
        } else if (amount >= 1000) {
            return String.format("%.1fK", amount / 1000.0);
        }
        return String.format("%,d", amount);
    }
    
    private void sendMessage(String player, String message) {
        apiAdapter.sendPrivateMessage(player, message);
    }
    
    private boolean isOnCooldown(String player) {
        Long lastCommand = playerCooldowns.get(player);
        if (lastCommand == null) return false;
        
        long cooldown = 3000; // 3 second cooldown
        return System.currentTimeMillis() - lastCommand < cooldown;
    }
    
    private void setCooldown(String player) {
        playerCooldowns.put(player, System.currentTimeMillis());
        
        // Clean up old cooldowns
        if (playerCooldowns.size() > 100) {
            long cutoff = System.currentTimeMillis() - 60000; // 1 minute
            playerCooldowns.entrySet().removeIf(entry -> entry.getValue() < cutoff);
        }
    }
    
    public void shutdown() {
        processedMessages.clear();
        playerCooldowns.clear();
        controller.log("ChatManager shut down");
    }
}