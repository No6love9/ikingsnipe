package com.ikingsnipe.core;

import com.ikingsnipe.casino.core.CasinoState;
import com.ikingsnipe.casino.gui.CasinoGUI;
import com.ikingsnipe.casino.listeners.TradeRequestListener;
import com.ikingsnipe.casino.managers.*;
import com.ikingsnipe.casino.models.CasinoConfig;
import com.ikingsnipe.casino.models.PlayerSession;
import com.ikingsnipe.casino.utils.ChatAI;
import com.ikingsnipe.casino.utils.DiscordWebhook;
import com.ikingsnipe.casino.utils.ProvablyFair;
import com.ikingsnipe.database.DatabaseManager;
import com.ikingsnipe.casino.games.GameResult;

import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.methods.trade.Trade;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.wrappers.widgets.message.Message;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * iKingSnipe GoatGang Casino - Main Application
 * Enterprise-grade OSRS casino bot with multiple games
 * 
 * Features:
 * - Multiple casino games (Craps, Dice Duel, Flower Poker, Blackjack, Hot/Cold)
 * - Database integration for player balances and statistics
 * - Provably fair RNG system
 * - Discord webhook notifications
 * - AI-powered chat responses
 * - Trade management with anti-scam protection
 * - Banking and muling automation
 * - Humanization and anti-ban features
 * 
 * @author ikingsnipe
 * @version 12.0.0
 */
@ScriptManifest(
    name = "iKingSnipe GoatGang Casino",
    author = "ikingsnipe",
    version = 12.0,
    description = "Enterprise casino bot with Craps, Dice, Flower Poker, and more. Full database integration.",
    category = Category.MONEYMAKING
)
public class BotApplication extends AbstractScript {
    
    // Core components
    private CasinoConfig config;
    private DatabaseManager dbManager;
    private GameManager gameManager;
    private TradeManager tradeManager;
    private BankingManager bankingManager;
    private LocationManager locationManager;
    private SessionManager sessionManager;
    private ProfitTracker profitTracker;
    private HumanizationManager humanizationManager;
    private MuleManager muleManager;
    private ChatAI chatAI;
    private DiscordWebhook discordWebhook;
    private ProvablyFair provablyFair;
    private TradeRequestListener tradeRequestListener;
    
    // State management
    private CasinoState currentState = CasinoState.INITIALIZING;
    private Map<String, PlayerSession> activeSessions = new ConcurrentHashMap<>();
    private String currentPlayer = null;
    private long currentBet = 0;
    private String currentGame = "craps";
    
    // Timing
    private long lastAdTime = 0;
    private long lastStateChange = 0;
    private long scriptStartTime = 0;
    
    // GUI
    private CasinoGUI gui;
    private boolean guiConfigured = false;

    @Override
    public void onStart() {
        try {
            // Security Authentication
            if (!SecurityManager.authenticate()) {
                Logger.log("[Security] Authentication failed. Closing script.");
                stop();
                return;
            }

            Logger.log("═══════════════════════════════════════════════");
            Logger.log("  GoatGang Edition by iKingSnipe - Starting");
            Logger.log("═══════════════════════════════════════════════");
            
            scriptStartTime = System.currentTimeMillis();
            
            // Load configuration
            config = new CasinoConfig();
            Logger.log("[Init] Configuration loaded.");
            
            // Initialize database
            String dbUrl = System.getenv("DB_URL");
            String dbUser = System.getenv("DB_USER");
            String dbPassword = System.getenv("DB_PASSWORD");
            
            if (dbUrl == null || dbUrl.isEmpty()) {
                dbUrl = "jdbc:mysql://localhost:3306/ikingsnipe_casino";
                dbUser = "root";
                dbPassword = "";
                Logger.log("[Init] Using default database configuration (fallback mode).");
            }
            
            dbManager = new DatabaseManager(dbUrl, dbUser, dbPassword);
            Logger.log("[Init] Database manager initialized.");
            
            // Initialize managers
            gameManager = new GameManager(config);
            tradeManager = new TradeManager(config, dbManager);
            bankingManager = new BankingManager(config);
            locationManager = new LocationManager(config);
            sessionManager = new SessionManager();
            profitTracker = new ProfitTracker();
            humanizationManager = new HumanizationManager(config);
            muleManager = new MuleManager(config);
            chatAI = new ChatAI(config);
            provablyFair = new ProvablyFair();
            
            // Initialize and register trade listener
            tradeRequestListener = new TradeRequestListener(tradeManager, config.tradeConfig);
            // In DreamBot, ChatListeners are registered via the script itself
            // The script class already implements ChatListener if we add it to the implements list
            // or we can manually register it if the API supports it.
            // For TradeRequestListener which implements ChatListener:
            
            if (config.discordEnabled && !config.discordWebhookUrl.isEmpty()) {
                discordWebhook = new DiscordWebhook(config.discordWebhookUrl);
                Logger.log("[Init] Discord webhook initialized.");
            }
            
            Logger.log("[Init] All managers initialized successfully.");
            
            // Show GUI
            SwingUtilities.invokeLater(() -> {
                gui = new CasinoGUI(config, (success) -> {
                    guiConfigured = true;
                    gameManager.syncSettings();
                    Logger.log("[GUI] Configuration applied. Starting casino operations...");
                });
                gui.setProfitTracker(profitTracker);
                gui.setVisible(true);
            });
            
            Logger.log("[Init] Waiting for GUI configuration...");
            while (!guiConfigured) {
                Sleep.sleep(500);
            }
            
            // Walk to location if configured
            if (config.walkOnStart) {
                changeState(CasinoState.WALKING_TO_LOCATION);
            } else {
                changeState(CasinoState.IDLE);
            }
            
            Logger.log("✓ Casino is now ONLINE and ready for players!");
            Logger.log("═══════════════════════════════════════════════");
            
        } catch (Exception e) {
            Logger.error("[CRITICAL] Startup failed: " + e.getMessage());
            e.printStackTrace();
            stop();
        }
    }

    @Override
    public int onLoop() {
        try {
            // Handle humanization
            if (humanizationManager != null && humanizationManager.shouldTakeBreak()) {
                changeState(CasinoState.BREAK);
                humanizationManager.takeBreak();
                changeState(CasinoState.IDLE);
            }
            
            // State machine
            switch (currentState) {
                case INITIALIZING:
                    return 1000;
                    
                case WALKING_TO_LOCATION:
                    locationManager.walkToLocation();
                    changeState(CasinoState.IDLE);
                    return 600;
                    
                case IDLE:
                    handleIdleState();
                    return 300;
                    
                case TRADING:
                    handleTradingState();
                    return 300;
                    
                case PLAYING_GAME:
                    handlePlayingGameState();
                    return 600;
                    
                case BANKING:
                    handleBankingState();
                    return 600;
                    
                case MULING:
                    handleMulingState();
                    return 600;
                    
                case BREAK:
                    return 5000;
                    
                default:
                    changeState(CasinoState.IDLE);
                    return 1000;
            }
            
        } catch (Exception e) {
            Logger.error("[Loop] Error: " + e.getMessage());
            e.printStackTrace();
            changeState(CasinoState.IDLE);
            return 1000;
        }
    }

    private void handleIdleState() {
        // Check if we need to bank
        if (!config.skipBanking && config.autoBank) {
            // Banking check would go here
        }
        
        // Check if we need to mule
        if (muleManager.shouldMule()) {
            changeState(CasinoState.MULING);
            return;
        }
        
        // Handle trade requests
        if (Trade.isOpen()) {
            changeState(CasinoState.TRADING);
            return;
        }
        
        // Advertise
        if (config.adMessages != null && !config.adMessages.isEmpty()) {
            long now = System.currentTimeMillis();
            if (now - lastAdTime > config.adIntervalSeconds * 1000L) {
                String ad = config.adMessages.get((int)(now % config.adMessages.size()));
                chatAI.sendMessage(ad, false);
                lastAdTime = now;
            }
        }
        
        // Random humanization actions would go here
    }

    private void handleTradingState() {
        if (!Trade.isOpen()) {
            changeState(CasinoState.IDLE);
            return;
        }
        
        if (Trade.isOpen(1)) {
            tradeManager.handleTradeScreen1();
        } else if (Trade.isOpen(2)) {
            tradeManager.handleTradeScreen2();
            // After successful trade, check if player wants to play
            if (!Trade.isOpen()) {
                String trader = Trade.getTradingWith();
                if (trader != null && !trader.isEmpty()) {
                    currentPlayer = trader;
                    // Player should use chat commands to play
                }
                changeState(CasinoState.IDLE);
            }
        }
    }

    private void handlePlayingGameState() {
        if (currentPlayer == null || currentBet <= 0) {
            Logger.log("[Game] Invalid game state. Resetting...");
            changeState(CasinoState.IDLE);
            return;
        }
        
        // Generate seed
        String seed = provablyFair.generateSeed(currentPlayer);
        
        // Play game
        GameResult result = gameManager.play(currentGame, currentPlayer, currentBet, seed);
        
        // Record in database
        dbManager.recordGame(
            currentPlayer,
            currentGame,
            currentBet,
            result.getPayout(),
            result.isWin() ? "WIN" : "LOSS",
            seed,
            result.getDescription()
        );
        
        // Update profit tracker
        if (result.isWin()) {
            profitTracker.recordLoss(result.getPayout() - currentBet);
        } else {
            profitTracker.recordWin(currentBet);
        }
        
        // Announce result
        String message;
        if (result.isWin()) {
            message = String.format(config.winMessage, 
                currentPlayer, 
                formatGP(result.getPayout()), 
                result.getDescription(),
                seed.substring(0, Math.min(8, seed.length())));
        } else {
            message = String.format(config.lossMessage, 
                currentPlayer, 
                result.getDescription());
        }
        chatAI.sendMessage(message, false);
        
        // Discord notification
        if (discordWebhook != null) {
            if ((result.isWin() && config.discordNotifyWins) || 
                (!result.isWin() && config.discordNotifyLosses)) {
                discordWebhook.sendGameResult(currentPlayer, currentGame, result, seed);
            }
        }
        
        // Reset and return to idle
        currentPlayer = null;
        currentBet = 0;
        changeState(CasinoState.IDLE);
    }

    private void handleBankingState() {
        if (bankingManager.restock()) {
            Logger.log("[Banking] Restock complete.");
            changeState(CasinoState.IDLE);
        }
    }

    private void handleMulingState() {
        if (muleManager.performMule()) {
            Logger.log("[Muling] Mule transfer complete.");
            changeState(CasinoState.IDLE);
        }
    }

    @Override
    public void onExit() {
        try {
            Logger.log("═══════════════════════════════════════════════");
            Logger.log("  iKingSnipe GoatGang Casino - Shutting Down");
            Logger.log("═══════════════════════════════════════════════");
            
            // Close GUI
            if (gui != null) {
                gui.dispose();
            }
            
            // Shutdown database
            if (dbManager != null) {
                dbManager.shutdown();
            }
            
            // Print statistics
            Logger.log("[Stats] Total Profit: " + formatGP(profitTracker.getTotalProfit()));
            Logger.log("[Stats] Total Wins: " + profitTracker.getTotalWins());
            Logger.log("[Stats] Total Losses: " + profitTracker.getTotalLosses());
            
            long runtime = System.currentTimeMillis() - scriptStartTime;
            Logger.log("[Stats] Runtime: " + formatTime(runtime));
            
            Logger.log("✓ Casino shutdown complete. Goodbye!");
            Logger.log("═══════════════════════════════════════════════");
            
        } catch (Exception e) {
            Logger.error("[Exit] Error during shutdown: " + e.getMessage());
        }
    }

    @Override
    public void onPaint(Graphics2D g) {
        try {
            // Paint overlay
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(5, 5, 250, 180);
            
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.drawString("iKingSnipe Casino v12.0", 15, 25);
            
            g.setFont(new Font("Arial", Font.PLAIN, 12));
            int y = 45;
            g.drawString("State: " + currentState, 15, y); y += 18;
            g.drawString("Game: " + currentGame, 15, y); y += 18;
            
            if (profitTracker != null) {
                g.drawString("Wins: " + profitTracker.getTotalWins(), 15, y); y += 18;
                g.drawString("Losses: " + profitTracker.getTotalLosses(), 15, y); y += 18;
                
                long profit = profitTracker.getTotalProfit();
                g.setColor(profit >= 0 ? Color.GREEN : Color.RED);
                g.drawString("Profit: " + formatGP(profit), 15, y); y += 18;
                g.setColor(Color.WHITE);
            }
            
            if (currentPlayer != null) {
                g.drawString("Player: " + currentPlayer, 15, y); y += 18;
                g.drawString("Bet: " + formatGP(currentBet), 15, y); y += 18;
            }
            
            long runtime = System.currentTimeMillis() - scriptStartTime;
            g.drawString("Runtime: " + formatTime(runtime), 15, y);
            
        } catch (Exception e) {
            // Ignore paint errors
        }
    }

    /**
     * Handle chat messages for game commands
     */
    public void onMessage(Message message) {
        if (tradeRequestListener != null) {
            tradeRequestListener.onMessage(message);
        }
        try {
            String text = message.getMessage();
            String sender = message.getUsername();
            
            if (sender == null || text == null) return;
            
            // Handle game commands
            if (text.startsWith(config.cmdCraps)) {
                handleGameCommand(sender, "craps", text);
            } else if (text.startsWith(config.cmdDiceDuel)) {
                handleGameCommand(sender, "dice", text);
            } else if (text.startsWith(config.cmdFlowerPoker)) {
                handleGameCommand(sender, "flower", text);
            } else if (text.startsWith(config.cmdBlackjack)) {
                handleGameCommand(sender, "blackjack", text);
            } else if (text.startsWith(config.cmdHotCold)) {
                handleGameCommand(sender, "hotcold", text);
            } else if (text.startsWith(config.cmdBalance)) {
                handleBalanceCommand(sender);
            } else if (text.startsWith(config.cmdStats)) {
                handleStatsCommand(sender);
            } else if (text.startsWith(config.cmdHelp)) {
                handleHelpCommand(sender);
            } else if (config.chatAIEnabled) {
                // AI response for other messages
                chatAI.handleMessage(sender, text);
            }
            
        } catch (Exception e) {
            Logger.error("[Message] Error handling message: " + e.getMessage());
        }
    }

    private void handleGameCommand(String player, String gameType, String command) {
        // Check if game is enabled
        if (!gameManager.isGameEnabled(gameType)) {
            chatAI.sendMessage(player + ", that game is currently disabled.", false);
            return;
        }
        
        // Parse bet amount from command
        String[] parts = command.trim().split("\\s+");
        long betAmount = config.minBet;
        
        if (parts.length > 1) {
            try {
                betAmount = parseAmount(parts[1]);
            } catch (Exception e) {
                chatAI.sendMessage(player + ", invalid bet amount. Use: " + command.split("\\s+")[0] + " <amount>", false);
                return;
            }
        }
        
        // Validate bet amount
        if (betAmount < config.minBet) {
            chatAI.sendMessage(player + ", minimum bet is " + formatGP(config.minBet), false);
            return;
        }
        
        if (betAmount > config.maxBet) {
            chatAI.sendMessage(player + ", maximum bet is " + formatGP(config.maxBet), false);
            return;
        }
        
        // Check player balance
        long balance = dbManager.getBalance(player);
        if (balance < betAmount) {
            chatAI.sendMessage(player + ", insufficient balance. Your balance: " + formatGP(balance), false);
            return;
        }
        
        // Deduct bet from balance
        dbManager.updateBalance(player, -betAmount);
        
        // Set up game
        currentPlayer = player;
        currentBet = betAmount;
        currentGame = gameType;
        
        changeState(CasinoState.PLAYING_GAME);
    }

    private void handleBalanceCommand(String player) {
        long balance = dbManager.getBalance(player);
        chatAI.sendMessage(player + ", your balance is: " + formatGP(balance), false);
    }

    private void handleStatsCommand(String player) {
        Map<String, Object> stats = dbManager.getPlayerStats(player);
        String msg = String.format("%s | Games: %d | Won: %d | Lost: %d | Balance: %s",
            player,
            stats.getOrDefault("games_played", 0),
            stats.getOrDefault("games_won", 0),
            stats.getOrDefault("games_lost", 0),
            formatGP((Long)stats.getOrDefault("balance", 0L)));
        chatAI.sendMessage(msg, false);
    }

    private void handleHelpCommand(String player) {
        String help = String.format(
            "Commands: %s <amt>, %s <amt>, %s <amt>, %s, %s | Min bet: %s",
            config.cmdCraps, config.cmdDiceDuel, config.cmdFlowerPoker,
            config.cmdBalance, config.cmdStats,
            formatGP(config.minBet));
        chatAI.sendMessage(help, false);
    }

    private void changeState(CasinoState newState) {
        if (currentState != newState) {
            Logger.log("[State] " + currentState + " -> " + newState);
            currentState = newState;
            lastStateChange = System.currentTimeMillis();
        }
    }

    private String formatGP(long amount) {
        if (amount >= 1_000_000_000) {
            return String.format("%.1fB", amount / 1_000_000_000.0);
        } else if (amount >= 1_000_000) {
            return String.format("%.1fM", amount / 1_000_000.0);
        } else if (amount >= 1_000) {
            return String.format("%.1fK", amount / 1_000.0);
        }
        return String.valueOf(amount);
    }

    private long parseAmount(String str) {
        str = str.toUpperCase().replaceAll("[^0-9KMB.]", "");
        double multiplier = 1;
        if (str.endsWith("K")) {
            multiplier = 1_000;
            str = str.substring(0, str.length() - 1);
        } else if (str.endsWith("M")) {
            multiplier = 1_000_000;
            str = str.substring(0, str.length() - 1);
        } else if (str.endsWith("B")) {
            multiplier = 1_000_000_000;
            str = str.substring(0, str.length() - 1);
        }
        return (long)(Double.parseDouble(str) * multiplier);
    }

    private String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        return String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60);
    }
}
