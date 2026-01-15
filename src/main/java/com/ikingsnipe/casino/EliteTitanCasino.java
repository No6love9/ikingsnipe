package com.ikingsnipe.casino;

import com.ikingsnipe.casino.games.AbstractGame;
import com.ikingsnipe.casino.games.GameResult;
import com.ikingsnipe.casino.games.impl.CrapsGame;
import com.ikingsnipe.casino.gui.CasinoGUI;
import com.ikingsnipe.casino.listeners.TradeRequestListener;
import com.ikingsnipe.casino.managers.*;
import com.ikingsnipe.casino.models.CasinoConfig;
import com.ikingsnipe.casino.models.CasinoState;
import com.ikingsnipe.casino.models.PlayerSession;
import com.ikingsnipe.casino.models.TradeStatistics;
import com.ikingsnipe.casino.utils.ChatAI;
import com.ikingsnipe.casino.utils.DiscordWebhook;
import com.ikingsnipe.casino.utils.ProvablyFair;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.input.Keyboard;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.trade.Trade;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.widgets.message.Message;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.script.listener.ChatListener;
import org.dreambot.api.script.listener.PaintListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

@ScriptManifest(name = "snipes♧scripts Enterprise", author = "ikingsnipe", version = 4.0, category = Category.MONEYMAKING, description = "Enterprise Casino System with Advanced Trade Handling")
public class EliteTitanCasino extends AbstractScript implements ChatListener, PaintListener, MouseListener {

    private CasinoConfig config;
    private CasinoState state = CasinoState.INITIALIZING;
    private GameManager gameManager;
    private SessionManager sessionManager;
    private BankingManager bankingManager;
    private LocationManager locationManager;
    private ProvablyFair provablyFair;
    private DiscordWebhook webhook;
    private ProfitTracker profitTracker;
    private ChatAI chatAI;
    private MuleManager muleManager;
    private HumanizationManager humanizationManager;
    
    // New trade handling components
    private TradeManager tradeManager;
    private TradeRequestListener tradeRequestListener;
    private TradeStatistics tradeStatistics;

    private String currentPlayer;
    private long currentBet;
    private String selectedGame = "dice";
    private PlayerSession currentSession;
    private long lastAdTime;
    private long tradeStartTime;
    private boolean welcomeSent;
    private volatile boolean guiFinished, startScript;
    
    private CasinoState lastState;
    private long lastStateChangeTime;
    private int adIndex = 0;
    
    // Payout tracking
    private int payoutAttempts = 0;
    private static final int MAX_PAYOUT_ATTEMPTS = 3;

    // Overlay Buttons
    private final Rectangle adBtn = new Rectangle(15, 200, 60, 20);
    private final Rectangle bankBtn = new Rectangle(80, 200, 60, 20);
    private final Rectangle resetBtn = new Rectangle(145, 200, 60, 20);

    @Override
    public void onStart() {
        config = new CasinoConfig();
        SwingUtilities.invokeLater(() -> {
            new CasinoGUI(config, (start) -> {
                this.startScript = start;
                this.guiFinished = true;
            });
        });

        sessionManager = new SessionManager();
        gameManager = new GameManager(config);
        bankingManager = new BankingManager(config);
        locationManager = new LocationManager(config);
        provablyFair = new ProvablyFair();
        profitTracker = new ProfitTracker();
        chatAI = new ChatAI();
        chatAI.setProfitTracker(profitTracker);
        muleManager = new MuleManager(config);
        humanizationManager = new HumanizationManager(config);
        
        // Initialize new trade handling components
        tradeStatistics = new TradeStatistics();
        tradeManager = new TradeManager(config, config.tradeConfig, sessionManager, provablyFair);
        tradeRequestListener = new TradeRequestListener(tradeManager, config.tradeConfig);
        
        if (config.discordEnabled && !config.discordWebhookUrl.isEmpty()) {
            webhook = new DiscordWebhook(config.discordWebhookUrl);
            webhook.send("🚀 **snipes♧scripts Enterprise v4.0** is now ONLINE! Advanced Trade Handling Active.");
        }
        state = CasinoState.INITIALIZING;
        
        log("snipes♧scripts Enterprise v4.0 initialized with Advanced Trade Handling");
    }

    @Override
    public int onLoop() {
        if (!guiFinished) return 500;
        if (!startScript) { stop(); return 0; }

        try {
            if (humanizationManager.shouldTakeBreak()) {
                humanizationManager.takeBreak();
                return 1000;
            }

            if (muleManager.shouldMule() || muleManager.isMulingInProgress()) {
                muleManager.handleMuling();
                return 1000;
            }

            // Stuck detection with state-specific timeouts
            if (state == lastState) {
                long stuckTimeout = getStateTimeout(state);
                if (System.currentTimeMillis() - lastStateChangeTime > stuckTimeout) {
                    log("Stuck detection triggered in state: " + state + ". Recovering...");
                    state = CasinoState.ERROR_RECOVERY;
                }
            } else {
                lastState = state;
                lastStateChangeTime = System.currentTimeMillis();
            }

            switch (state) {
                case INITIALIZING: return handleInitializing();
                case WALKING_TO_LOCATION: return handleWalking();
                case BANKING: return handleBanking();
                case IDLE: return handleIdle();
                case TRADING_WINDOW_1: return handleTrade1();
                case TRADING_WINDOW_2: return handleTrade2();
                case PROCESSING_GAME: return handleGame();
                case PAYOUT_PENDING: return handlePayout();
                case ERROR_RECOVERY: return handleRecovery();
            }
        } catch (Exception e) {
            log("Critical Error: " + e.getMessage());
            e.printStackTrace();
            state = CasinoState.ERROR_RECOVERY;
        }
        return Calculations.random(200, 400);
    }
    
    /**
     * Get appropriate timeout for each state
     */
    private long getStateTimeout(CasinoState state) {
        switch (state) {
            case TRADING_WINDOW_1:
            case TRADING_WINDOW_2:
                return config.tradeConfig.tradeTimeout;
            case PAYOUT_PENDING:
                return config.tradeConfig.payoutTradeTimeout * MAX_PAYOUT_ATTEMPTS;
            case PROCESSING_GAME:
                return 30000;
            default:
                return 120000; // 2 minutes default
        }
    }

    private int handleInitializing() {
        if (config.walkOnStart && !locationManager.isAtLocation()) {
            state = CasinoState.WALKING_TO_LOCATION;
        } else if (config.autoBank && Inventory.count(CasinoConfig.COINS_ID) < config.restockThreshold) {
            state = CasinoState.BANKING;
        } else {
            state = CasinoState.IDLE;
        }
        return 500;
    }

    private int handleWalking() {
        if (locationManager.isAtLocation()) { state = CasinoState.INITIALIZING; return 500; }
        locationManager.walkToLocation();
        return 1000;
    }

    private int handleBanking() {
        if (bankingManager.restock()) {
            state = CasinoState.INITIALIZING;
        }
        return 1000;
    }

    /**
     * Enhanced IDLE state with comprehensive trade detection
     */
    private int handleIdle() {
        // Use TradeManager for comprehensive trade detection
        TradeManager.TradeDetectionResult detection = tradeManager.detectTradeRequest();
        
        if (detection.detected) {
            currentPlayer = detection.playerName;
            
            if (currentPlayer != null) {
                log("[TradeDetection] Trade detected with: " + currentPlayer + " (Type: " + detection.type + ")");
                
                // Initialize trade session
                tradeManager.initializeTradeSession(currentPlayer);
                currentSession = sessionManager.getSession(currentPlayer);
                tradeStartTime = System.currentTimeMillis();
                welcomeSent = false;
                selectedGame = config.defaultGame;
                
                // Record trade attempt in statistics
                if (config.tradeConfig.trackTradeStats) {
                    tradeStatistics.recordTradeAttempt(currentPlayer);
                }
                
                state = CasinoState.TRADING_WINDOW_1;
                return 300;
            }
        }
        
        // Check for pending trade requests from listener
        if (tradeRequestListener.hasPendingRequests()) {
            TradeRequestListener.TradeRequestEvent request = tradeRequestListener.getNextRequest();
            if (request != null) {
                log("[TradeDetection] Processing queued request from: " + request.playerName);
                Player requester = Players.closest(request.playerName);
                if (requester != null && !Trade.isOpen()) {
                    requester.interact("Trade with");
                    Sleep.sleepUntil(Trade::isOpen, config.tradeConfig.tradeAcceptTimeout);
                }
            }
        }

        // Apply humanization during idle
        humanizationManager.applyIdleHumanization();
        
        // Random location adjustment
        if (Calculations.random(1, 100) == 1) {
            locationManager.walkToLocation();
        }

        // Advertising
        if (System.currentTimeMillis() - lastAdTime > config.adIntervalSeconds * 1000L) {
            sendRotatingAd();
            lastAdTime = System.currentTimeMillis();
        }
        
        return Calculations.random(800, 1200);
    }

    private void sendRotatingAd() {
        if (config.adMessages.isEmpty()) return;
        String msg = config.adMessages.get(adIndex);
        if (config.enableAntiMute) {
            msg += " " + generateRandomString(3);
        }
        Keyboard.type(msg, true);
        adIndex = (adIndex + 1) % config.adMessages.size();
    }

    private String generateRandomString(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(Calculations.random(0, chars.length() - 1)));
        }
        return sb.toString();
    }

    /**
     * Enhanced Trade Screen 1 handling with anti-scam verification
     */
    private int handleTrade1() {
        if (!Trade.isOpen(1)) { 
            if (!Trade.isOpen()) {
                log("[Trade1] Trade closed unexpectedly");
                tradeStatistics.recordFailedTrade(currentPlayer, "Trade closed");
                reset(); 
            }
            return 500; 
        }
        
        // Check for trade timeout
        if (System.currentTimeMillis() - tradeStartTime > config.tradeConfig.tradeTimeout) {
            log("[Trade1] Trade timed out with: " + currentPlayer);
            Keyboard.type("Trade timed out. Please trade again!", true);
            Trade.declineTrade();
            tradeStatistics.recordDeclinedTrade(currentPlayer, "Timeout");
            reset();
            return 1000;
        }

        // Send welcome message if not sent
        if (!welcomeSent) {
            sendWelcomeMessage();
            welcomeSent = true;
            return 500;
        }
        
        long tradeVal = getTradeValue();
        
        // Handle case where player is using existing balance
        if (currentBet > 0 && tradeVal == 0 && currentSession.getBalance() >= currentBet) {
            confirmToClanChat();
            if (acceptTradeScreen1()) {
                return 500;
            }
        } 
        // Handle new bet from trade
        else if (tradeVal > 0) {
            // Validate bet limits
            if (tradeVal < config.minBet || tradeVal > config.maxBet) {
                String limitMsg = "Bet must be between " + formatGP(config.minBet) + " and " + formatGP(config.maxBet);
                Keyboard.type(limitMsg, true);
                Trade.declineTrade();
                tradeStatistics.recordDeclinedTrade(currentPlayer, "Outside bet limits");
                reset();
                return 1000;
            }
            
            // Anti-Scam Verification: Check value stability
            if (verifyTradeValue(tradeVal)) {
                currentBet = tradeVal;
                tradeManager.setCurrentBetAmount(tradeVal);
                
                // Send confirmation
                if (config.tradeConfig.sendConfirmationMessages) {
                    Keyboard.type("Bet confirmed: " + formatGP(currentBet) + ". Good luck!", true);
                    Sleep.sleep(300, 500);
                }
                
                confirmToClanChat();
                if (acceptTradeScreen1()) {
                    return 500;
                }
            }
        }
        
        return Calculations.random(400, 600);
    }
    
    /**
     * Send welcome message to trader
     */
    private void sendWelcomeMessage() {
        String greeting = config.tradeConfig.customWelcomeMessage
            .replace("{player}", currentPlayer != null ? currentPlayer : "Player")
            .replace("{hash}", provablyFair.getHash().substring(0, 12))
            .replace("{min}", formatGP(config.minBet))
            .replace("{max}", formatGP(config.maxBet));
        
        Keyboard.type(greeting, true);
        Sleep.sleep(400, 600);
        
        if (config.tradeConfig.sendGameCommands) {
            Keyboard.type("Commands: !c (Craps) !dw (Dice War) !fp (Flower Poker) !b2b", true);
        }
    }
    
    /**
     * Verify trade value hasn't changed (anti-scam)
     */
    private long lastVerifiedValue = 0;
    private int verificationCount = 0;
    private long lastValueChangeTime = 0;
    
    private boolean verifyTradeValue(long currentValue) {
        long now = System.currentTimeMillis();
        
        // If value changed, reset verification
        if (currentValue != lastVerifiedValue) {
            if (lastVerifiedValue > 0 && config.tradeConfig.enableAntiScam) {
                log("[AntiScam] Value changed from " + lastVerifiedValue + " to " + currentValue);
                tradeStatistics.recordScamAttempt(currentPlayer, "VALUE_CHANGE");
            }
            lastVerifiedValue = currentValue;
            lastValueChangeTime = now;
            verificationCount = 0;
            return false;
        }
        
        // Check if value has been stable long enough
        long stableTime = now - lastValueChangeTime;
        if (stableTime >= config.tradeConfig.valueStabilityTime) {
            verificationCount++;
            
            // Get required verification count based on value
            int requiredChecks = getRequiredVerificationChecks(currentValue);
            
            if (verificationCount >= requiredChecks) {
                // Final verification delay
                Sleep.sleep(config.tradeConfig.minVerifyDelay, config.tradeConfig.maxVerifyDelay);
                
                // Double-check value hasn't changed
                long finalCheck = getTradeValue();
                if (finalCheck != currentValue) {
                    log("[AntiScam] Final check failed! Expected: " + currentValue + ", Got: " + finalCheck);
                    tradeStatistics.recordScamAttempt(currentPlayer, "FINAL_CHECK_FAIL");
                    Keyboard.type("Please don't change the trade amount!", true);
                    verificationCount = 0;
                    return false;
                }
                
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get required verification checks based on bet size
     */
    private int getRequiredVerificationChecks(long value) {
        if (!config.tradeConfig.enableAntiScam) return 1;
        
        // Check if player is trusted (reduced verification)
        if (config.tradeConfig.reducedVerifyForTrusted && 
            tradeStatistics.isPlayerTrusted(currentPlayer, config.tradeConfig.trustedPlayerTradeCount)) {
            return 1;
        }
        
        if (value >= config.tradeConfig.highValueThreshold) {
            return config.tradeConfig.highValueVerifyCount;
        } else if (value >= config.tradeConfig.mediumValueThreshold) {
            return config.tradeConfig.mediumValueVerifyCount;
        }
        return config.tradeConfig.lowValueVerifyCount;
    }
    
    /**
     * Accept trade screen 1 and transition to screen 2
     */
    private boolean acceptTradeScreen1() {
        if (Trade.acceptTrade()) {
            boolean screen2Opened = Sleep.sleepUntil(() -> Trade.isOpen(2), config.tradeConfig.screen2WaitTime);
            if (screen2Opened) {
                state = CasinoState.TRADING_WINDOW_2;
                return true;
            }
        }
        return false;
    }

    /**
     * Enhanced Trade Screen 2 handling with double-check verification
     */
    private long screen2StartTime = 0;
    private boolean screen2Verified = false;
    
    private int handleTrade2() {
        if (!Trade.isOpen(2)) { 
            if (!Trade.isOpen()) {
                log("[Trade2] Trade closed unexpectedly");
                tradeStatistics.recordFailedTrade(currentPlayer, "Trade closed on screen 2");
                reset(); 
            } else if (Trade.isOpen(1)) {
                // Went back to screen 1
                log("[Trade2] Returned to screen 1");
                screen2Verified = false;
                state = CasinoState.TRADING_WINDOW_1;
            }
            return 500; 
        }
        
        // Initialize screen 2 timer on first entry
        if (screen2StartTime == 0) {
            screen2StartTime = System.currentTimeMillis();
        }
        
        // Check for screen 2 timeout
        if (System.currentTimeMillis() - screen2StartTime > config.tradeConfig.screen2Timeout) {
            log("[Trade2] Screen 2 timed out");
            Keyboard.type("Trade confirmation timed out!", true);
            Trade.declineTrade();
            tradeStatistics.recordDeclinedTrade(currentPlayer, "Screen 2 timeout");
            reset();
            return 1000;
        }
        
        // Perform screen 2 verification if enabled
        if (!screen2Verified && config.tradeConfig.enableScreen2Verification) {
            Sleep.sleep(config.tradeConfig.screen2VerifyDelay, config.tradeConfig.screen2VerifyDelay + 100);
            
            // Verify the value matches what we accepted in screen 1
            long screen2Value = getTradeValue();
            if (screen2Value != lastVerifiedValue && lastVerifiedValue > 0) {
                log("[AntiScam] Screen 2 value mismatch! Expected: " + lastVerifiedValue + ", Got: " + screen2Value);
                tradeStatistics.recordScamAttempt(currentPlayer, "SCREEN2_MISMATCH");
                Keyboard.type("Trade verification failed. Please try again.", true);
                Trade.declineTrade();
                tradeStatistics.recordDeclinedTrade(currentPlayer, "Screen 2 mismatch");
                reset();
                return 1000;
            }
            screen2Verified = true;
        }
        
        // Accept trade
        if (Trade.acceptTrade()) {
            boolean tradeComplete = Sleep.sleepUntil(() -> !Trade.isOpen(), config.tradeConfig.tradeCompleteWaitTime);
            if (tradeComplete) {
                log("[Trade2] Trade completed successfully with: " + currentPlayer);
                
                // Record successful trade
                long tradeDuration = System.currentTimeMillis() - tradeStartTime;
                tradeStatistics.recordCompletedTrade(currentPlayer, currentBet, tradeDuration, false);
                
                state = CasinoState.PROCESSING_GAME;
            }
        }
        
        return Calculations.random(400, 600);
    }

    private int handleGame() {
        AbstractGame game = gameManager.getGame(selectedGame);
        if (game == null) {
            log("[Game] Game not found: " + selectedGame + ", defaulting to dice");
            game = gameManager.getGame("dice");
        }
        
        double mult = config.games.get(selectedGame) != null ? 
            config.games.get(selectedGame).multiplier : 2.0;
        GameResult result = game.play(currentBet, mult);
        
        String logMsg = result.isWin() ? 
            String.format(config.winMessage, currentPlayer, formatGP(result.getPayout()), result.getDescription(), provablyFair.getSeed().substring(0, 8)) :
            String.format(config.lossMessage, currentPlayer, result.getDescription());
        
        Keyboard.type(logMsg, true);
        
        // Clan Chat Broadcast for Big Wins
        if (result.isWin() && currentBet >= 10_000_000L) {
            Sleep.sleep(500, 800);
            String clanMsg = String.format("/HUGE WIN! %s just won %s on %s!", currentPlayer, formatGP(result.getPayout()), selectedGame);
            Keyboard.type(clanMsg, true);
        }

        profitTracker.addGame(currentPlayer, result.isWin(), result.isWin() ? result.getPayout() - currentBet : -currentBet);
        
        // Update trade statistics
        if (config.tradeConfig.trackTradeStats) {
            // Already recorded as completed, update win status
        }
        
        if (webhook != null) {
            webhook.sendGameResult(currentPlayer, result.isWin(), currentBet, result.getPayout(), result.getDescription(), provablyFair.getSeed(), config);
        }

        if (result.isWin()) {
            currentSession.addBalance(result.getPayout());
            payoutAttempts = 0;
            state = CasinoState.PAYOUT_PENDING;
        } else {
            reset();
        }
        return 1000;
    }

    /**
     * Enhanced payout handling with retry logic
     */
    private int handlePayout() {
        Player p = Players.closest(currentPlayer);
        
        if (p == null) {
            log("[Payout] Player " + currentPlayer + " not found. Attempt " + (payoutAttempts + 1) + "/" + MAX_PAYOUT_ATTEMPTS);
            payoutAttempts++;
            if (payoutAttempts >= MAX_PAYOUT_ATTEMPTS) {
                log("[Payout] Max attempts reached. Player may have left.");
                Keyboard.type(currentPlayer + ", please trade me to collect your " + formatGP(currentSession.getBalance()) + " winnings!", true);
                // Keep session balance for when they return
                reset();
            }
            return 2000;
        }
        
        if (!Trade.isOpen()) {
            // Initiate trade with winner
            Sleep.sleep(config.tradeConfig.payoutInitDelay, config.tradeConfig.payoutInitDelay + 500);
            p.interact("Trade with");
            if (Sleep.sleepUntil(Trade::isOpen, config.tradeConfig.tradeAcceptTimeout)) {
                Sleep.sleep(400, 600);
            } else {
                payoutAttempts++;
                return 1000;
            }
        }
        
        if (Trade.isOpen(1)) {
            long toPay = currentSession.getBalance();
            addPayoutItems(toPay);
            Sleep.sleep(300, 500);
            
            if (config.tradeConfig.sendConfirmationMessages) {
                Keyboard.type("Paying out " + formatGP(toPay) + ". GG!", true);
            }
            
            Trade.acceptTrade();
            Sleep.sleepUntil(() -> Trade.isOpen(2), config.tradeConfig.screen2WaitTime);
        } else if (Trade.isOpen(2)) {
            if (Trade.acceptTrade()) {
                boolean complete = Sleep.sleepUntil(() -> !Trade.isOpen(), config.tradeConfig.tradeCompleteWaitTime);
                if (complete) {
                    log("[Payout] Payout completed: " + formatGP(currentSession.getBalance()) + " to " + currentPlayer);
                    tradeStatistics.recordPayout(currentPlayer, currentSession.getBalance());
                    currentSession.setBalance(0);
                    reset();
                }
            }
        }
        
        return Calculations.random(600, 1000);
    }

    private void addPayoutItems(long amount) {
        long rem = amount;
        if (rem >= 1000) {
            int tokens = (int)(rem / 1000);
            int available = Inventory.count(CasinoConfig.PLATINUM_TOKEN_ID);
            int toAdd = Math.min(tokens, available);
            if (toAdd > 0) {
                Trade.addItem(CasinoConfig.PLATINUM_TOKEN_ID, toAdd);
                rem -= (long)toAdd * 1000L;
                Sleep.sleep(200, 400);
            }
        }
        if (rem > 0) {
            int available = Inventory.count(CasinoConfig.COINS_ID);
            int toAdd = (int)Math.min(rem, available);
            if (toAdd > 0) {
                Trade.addItem(CasinoConfig.COINS_ID, toAdd);
            }
        }
    }

    private int handleRecovery() { 
        log("[Recovery] Entering recovery mode");
        Widgets.closeAll(); 
        if (Trade.isOpen()) Trade.declineTrade(); 
        Sleep.sleep(500, 1000);
        reset(); 
        return 1000; 
    }

    private void confirmToClanChat() {
        String msg = String.format("/Confirming %s's bet of %s. Safe to accept!", currentPlayer, formatGP(currentBet));
        Keyboard.type(msg, true);
    }

    @Override
    public void onMessage(Message msg) {
        // Forward to trade request listener
        if (tradeRequestListener != null) {
            tradeRequestListener.onMessage(msg);
        }
        
        String txt = msg.getMessage().toLowerCase();
        String sender = msg.getUsername();
        
        if (config.chatAIEnabled && !sender.equals(Players.getLocal().getName())) {
            chatAI.handleChat(txt);
        }

        // Handle game commands during trade
        if (state == CasinoState.TRADING_WINDOW_1 && sender.equals(currentPlayer)) {
            if (txt.startsWith("!c") || txt.startsWith("!craps")) { 
                selectedGame = "craps"; 
                tradeManager.setSelectedGame("craps");
                parseBet(txt); 
                Keyboard.type("Game set to Chasing Craps! Good luck!", true);
            }
            else if (txt.startsWith("!dw") || txt.startsWith("!dicewar")) { 
                selectedGame = "dicewar"; 
                tradeManager.setSelectedGame("dicewar");
                parseBet(txt); 
                Keyboard.type("Game set to Dice War! Let's go!", true);
            }
            else if (txt.startsWith("!b2b")) {
                selectedGame = "craps";
                tradeManager.setSelectedGame("craps");
                CrapsGame cg = (CrapsGame) gameManager.getGame("craps");
                if (cg != null) {
                    cg.setConfig(config);
                    cg.setB2B(true);
                    if (txt.contains("7")) cg.setPredictedNumber(7);
                    else if (txt.contains("9")) cg.setPredictedNumber(9);
                    else if (txt.contains("12")) cg.setPredictedNumber(12);
                }
                parseBet(txt);
                Keyboard.type("B2B Chasing Craps activated! Let's hit it!", true);
            }
            else if (txt.startsWith("!fp") || txt.startsWith("!flower")) {
                selectedGame = "flower";
                tradeManager.setSelectedGame("flower");
                parseBet(txt);
                Keyboard.type("Game set to Flower Poker! Plant those seeds!", true);
            }
            else if (txt.startsWith("!dice") || txt.startsWith("!dd")) {
                selectedGame = "dice";
                tradeManager.setSelectedGame("dice");
                parseBet(txt);
                Keyboard.type("Game set to Dice Duel!", true);
            }
            else if (txt.startsWith("!55") || txt.startsWith("!55x2")) {
                selectedGame = "55x2";
                tradeManager.setSelectedGame("55x2");
                parseBet(txt);
                Keyboard.type("Game set to 55x2!", true);
            }
            else if (txt.startsWith("!hc") || txt.startsWith("!hotcold")) {
                selectedGame = "hotcold";
                tradeManager.setSelectedGame("hotcold");
                parseBet(txt);
                Keyboard.type("Game set to Hot/Cold!", true);
            }
        }
    }

    private void parseBet(String txt) {
        String[] parts = txt.split(" ");
        if (parts.length > 1) {
            try {
                String val = parts[1].toLowerCase();
                if (val.endsWith("k")) currentBet = Long.parseLong(val.replace("k", "")) * 1000L;
                else if (val.endsWith("m")) currentBet = Long.parseLong(val.replace("m", "")) * 1000000L;
                else if (val.endsWith("b")) currentBet = Long.parseLong(val.replace("b", "")) * 1000000000L;
                else currentBet = Long.parseLong(val);
                tradeManager.setCurrentBetAmount(currentBet);
            } catch (Exception ignored) {}
        }
    }

    private void reset() {
        currentPlayer = null;
        currentBet = 0;
        state = CasinoState.IDLE;
        welcomeSent = false;
        lastVerifiedValue = 0;
        verificationCount = 0;
        lastValueChangeTime = 0;
        screen2StartTime = 0;
        screen2Verified = false;
        payoutAttempts = 0;
        tradeManager.resetTradeState();
    }

    private long getTradeValue() {
        long total = 0;
        try {
            for (Item item : Trade.getTheirItems()) {
                if (item != null) {
                    if (item.getID() == CasinoConfig.PLATINUM_TOKEN_ID) {
                        total += item.getAmount() * 1000L;
                    } else if (item.getID() == CasinoConfig.COINS_ID) {
                        total += item.getAmount();
                    }
                }
            }
        } catch (Exception e) {
            log("Error getting trade value: " + e.getMessage());
        }
        return total;
    }

    private String formatGP(long a) {
        if (a >= 1_000_000_000) return String.format("%.1fB", a / 1_000_000_000.0);
        if (a >= 1_000_000) return String.format("%.1fM", a / 1_000_000.0);
        if (a >= 1_000) return String.format("%.1fK", a / 1_000.0);
        return String.valueOf(a);
    }

    @Override
    public void onPaint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Main panel
        g2.setColor(new Color(0, 0, 0, 200));
        g2.fillRoundRect(10, 10, 240, 220, 15, 15);
        g2.setColor(new Color(0, 255, 127));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(10, 10, 240, 220, 15, 15);
        
        // Title
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.drawString("snipes♧scripts Enterprise v4.0", 15, 30);
        
        g2.setFont(new Font("Arial", Font.PLAIN, 11));
        g2.setColor(Color.WHITE);
        
        int y = 48;
        g2.drawString("Runtime: " + profitTracker.getRuntime(), 15, y); y += 16;
        g2.drawString("Net Profit: " + formatGP(profitTracker.getNetProfit()), 15, y); y += 16;
        g2.drawString("Jackpot: " + formatGP(config.currentJackpot), 15, y); y += 16;
        
        // Trade statistics
        g2.setColor(new Color(0, 255, 127));
        g2.drawString("Trade Stats:", 15, y); y += 14;
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        g2.drawString("Completed: " + tradeStatistics.getTotalTradesCompleted() + 
                     " | Success: " + String.format("%.1f%%", tradeStatistics.getTradeSuccessRate()), 15, y); y += 13;
        g2.drawString("Scam Blocked: " + tradeStatistics.getScamAttemptsDetected() + 
                     " | Avg Time: " + (tradeStatistics.getAverageTradeTime() / 1000) + "s", 15, y); y += 16;
        
        // Status
        g2.setFont(new Font("Arial", Font.PLAIN, 11));
        String status = state.getStatus();
        if (state == CasinoState.BANKING) {
            status = bankingManager.getStatus();
        } else if (state == CasinoState.TRADING_WINDOW_1 || state == CasinoState.TRADING_WINDOW_2) {
            status += " (" + currentPlayer + ")";
        }
        g2.drawString("Status: " + status, 15, y); y += 16;
        
        // Current trade info
        if (currentPlayer != null && currentBet > 0) {
            g2.setColor(new Color(255, 215, 0));
            g2.drawString("Trading: " + currentPlayer + " - " + formatGP(currentBet), 15, y);
            y += 14;
        }
        
        // Recent winners
        g2.setColor(new Color(0, 255, 127, 150));
        g2.drawString("Recent Winners:", 15, y); y += 13;
        g2.setFont(new Font("Arial", Font.ITALIC, 10));
        g2.setColor(Color.LIGHT_GRAY);
        List<String> winners = profitTracker.getRecentWinners();
        for (int i = 0; i < Math.min(winners.size(), 2); i++) {
            g2.drawString("• " + winners.get(i), 18, y);
            y += 12;
        }
        
        // Buttons
        g2.setFont(new Font("Arial", Font.BOLD, 10));
        drawButton(g2, adBtn, "AD", new Color(0, 150, 255));
        drawButton(g2, bankBtn, "BANK", new Color(255, 165, 0));
        drawButton(g2, resetBtn, "RESET", new Color(255, 80, 80));
    }
    
    private void drawButton(Graphics2D g2, Rectangle rect, String text, Color color) {
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 150));
        g2.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 5, 5);
        g2.setColor(color);
        g2.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 5, 5);
        g2.setColor(Color.WHITE);
        FontMetrics fm = g2.getFontMetrics();
        int textX = rect.x + (rect.width - fm.stringWidth(text)) / 2;
        int textY = rect.y + ((rect.height - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(text, textX, textY);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        Point p = e.getPoint();
        if (adBtn.contains(p)) {
            sendRotatingAd();
            lastAdTime = System.currentTimeMillis();
        } else if (bankBtn.contains(p)) {
            state = CasinoState.BANKING;
        } else if (resetBtn.contains(p)) {
            if (Trade.isOpen()) Trade.declineTrade();
            reset();
        }
    }

    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}
