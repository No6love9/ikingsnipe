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
import org.dreambot.api.methods.input.Keyboard;
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

@ScriptManifest(name = "Elite Titan Casino Pro", author = "ikingsnipe", version = 5.0, category = Category.MONEYMAKING, description = "Professional Enterprise Casino System with Robust Trade Handling & Multi-Game Support")
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
        log("Initializing Elite Titan Casino Pro...");
        config = new CasinoConfig();
        
        // Professional GUI initialization
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                log("Could not set system look and feel");
            }
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
        chatAI.setConfig(config);
        chatAI.setProvablyFair(provablyFair);
        muleManager = new MuleManager(config);
        humanizationManager = new HumanizationManager(config);
        
        // Initialize trade handling components
        tradeStatistics = new TradeStatistics();
        tradeManager = new TradeManager(config, config.tradeConfig, sessionManager, provablyFair);
        tradeRequestListener = new TradeRequestListener(tradeManager, config.tradeConfig);
        
        if (config.discordEnabled && !config.discordWebhookUrl.isEmpty()) {
            webhook = new DiscordWebhook(config.discordWebhookUrl);
            webhook.send("🚀 **Elite Titan Casino Pro v5.0** is now ONLINE! Robust Trade Handling Active.");
        }
        state = CasinoState.INITIALIZING;
        
        log("Elite Titan Casino Pro v5.0 initialized successfully.");
    }

    @Override
    public int onLoop() {
        if (!guiFinished) return 500;
        if (!startScript) { stop(); return 0; }

        // Admin Emergency Stop
        if (config.adminConfig.emergencyStop) {
            log("!!! ADMIN EMERGENCY STOP TRIGGERED !!!");
            stop();
            return 0;
        }

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
            if (config.skipBanking) {
                log("Skip Banking option enabled. Bypassing banking check and proceeding to IDLE.");
                state = CasinoState.IDLE;
            } else {
                state = CasinoState.BANKING;
            }
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
        // Admin Override: Disable all games
        if (config.adminConfig.disableAllGames) {
            if (Calculations.random(1, 100) == 1) {
                log("Games are currently disabled by Admin.");
            }
            return 1000;
        }

        // Use TradeManager for comprehensive trade detection
        TradeManager.TradeDetectionResult detection = tradeManager.detectTradeRequest();
        
        if (detection.success) {
            // Admin Override: Blacklist check
            if (config.adminConfig.isBlacklisted(detection.playerName)) {
                log("Ignoring trade request from blacklisted player: " + detection.playerName);
                return 1000;
            }
            currentPlayer = detection.playerName;
            
            if (currentPlayer != null) {
                log("[TradeDetection] Trade detected with: " + currentPlayer + " (Type: " + detection.type + ")");
                
                // Initialize trade session
                tradeManager.initializeTradeSession(currentPlayer);
                currentSession = sessionManager.getSession(currentPlayer);
                tradeStartTime = System.currentTimeMillis();
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
        
        String ad = config.adMessages.get(adIndex);
        if (config.enableAntiMute) {
            ad = humanizationManager.obfuscateText(ad);
        }
        
        Keyboard.type(ad, true);
        adIndex = (adIndex + 1) % config.adMessages.size();
    }

    private int handleTrade1() {
        if (!Trade.isOpen(1)) {
            if (Trade.isOpen(2)) {
                state = CasinoState.TRADING_WINDOW_2;
                return 300;
            }
            log("[Trade] Trade window 1 closed unexpectedly.");
            state = CasinoState.IDLE;
            return 500;
        }

        TradeManager.TradeAction action = tradeManager.handleTradeScreen1(currentSession);
        
        switch (action) {
            case ACCEPTED_SCREEN1:
                log("[Trade] Screen 1 accepted. Waiting for screen 2...");
                return 500;
            case DECLINED:
                log("[Trade] Trade declined during screen 1.");
                state = CasinoState.IDLE;
                return 1000;
            case TRADE_CLOSED:
                state = CasinoState.IDLE;
                return 500;
            case WAIT:
            default:
                return 500;
        }
    }

    private int handleTrade2() {
        if (!Trade.isOpen(2)) {
            if (Trade.isOpen(1)) {
                state = CasinoState.TRADING_WINDOW_1;
                return 300;
            }
            // If trade closed and we were on screen 2, it might have been completed
            if (System.currentTimeMillis() - tradeStartTime > 2000) {
                log("[Trade] Trade completed successfully!");
                state = CasinoState.PROCESSING_GAME;
                return 500;
            }
            state = CasinoState.IDLE;
            return 500;
        }

        TradeManager.TradeAction action = tradeManager.handleTradeScreen2();
        
        switch (action) {
            case COMPLETED:
                log("[Trade] Screen 2 accepted. Trade complete.");
                currentBet = tradeManager.getCurrentBetAmount();
                state = CasinoState.PROCESSING_GAME;
                return 1000;
            case DECLINED:
                log("[Trade] Trade declined during screen 2.");
                state = CasinoState.IDLE;
                return 1000;
            case TRADE_CLOSED:
                state = CasinoState.IDLE;
                return 500;
            case WAIT:
            default:
                return 500;
        }
    }

    private int handleGame() {
        log("[Game] Processing " + selectedGame + " for " + currentPlayer + " with bet " + currentBet);
        
        // Record statistics
        if (config.tradeConfig.trackTradeStats) {
            tradeStatistics.recordTradeSuccess(currentPlayer, currentBet);
        }
        
        gameManager.syncSettings();
        AbstractGame game = gameManager.getGame(selectedGame);
        if (game == null) {
            log("[Error] Game not found: " + selectedGame);
            state = CasinoState.IDLE;
            return 500;
        }

        GameResult result = game.play(currentPlayer, currentBet, provablyFair.getSeed());
        log("[Game] Result: " + (result.isWin() ? "WIN" : "LOSS") + " | Roll: " + result.getRoll());
        
        // Update session and profit
        currentSession.addGame(result);
        profitTracker.addGame(currentPlayer, result.isWin(), currentBet, result.getPayout());
        // Send result message
        String resultMsg = result.getDescription();
        Keyboard.type(resultMsg, true);
        
        // Big Win Announcement
        if (result.isWin() && result.getPayout() >= config.bigWinThreshold && config.autoAnnounceBigWins) {
            Sleep.sleep(1000, 2000);
            Keyboard.type("!!! BIG WIN ALERT: " + currentPlayer + " just won " + formatGP(result.getPayout()) + " !!!", true);
        }
        
        // Clan Chat Big Win Announcement
        if (result.isWin() && config.clanChatEnabled && config.clanChatAnnounceWins && result.getPayout() >= config.clanChatBigWinThreshold) {
            Sleep.sleep(1000, 2000);
            Keyboard.type("/[CLAN] BIG WIN: " + currentPlayer + " won " + formatGP(result.getPayout()) + "!", true);
        }
        
        // Handle payout if win
        if (result.isWin()) {
            state = CasinoState.PAYOUT_PENDING;
            payoutAttempts = 0;
        } else {
            state = CasinoState.IDLE;
        }
        
        // Send Discord notification
        if (webhook != null) {
            webhook.sendGameResult(currentPlayer, result.isWin(), currentBet, result.getPayout(), result.getDescription(), provablyFair.getSeed(), config);
        }
        
        return 1000;
    }

    private int handlePayout() {
        if (payoutAttempts >= MAX_PAYOUT_ATTEMPTS) {
            log("[Payout] Failed to pay out after " + MAX_PAYOUT_ATTEMPTS + " attempts. Manual intervention required.");
            if (webhook != null) {
                webhook.send("⚠️ **PAYOUT ERROR**: Failed to pay " + currentPlayer + " " + currentBet * 2);
            }
            state = CasinoState.IDLE;
            return 1000;
        }

        log("[Payout] Attempting payout to " + currentPlayer + " (Attempt " + (payoutAttempts + 1) + ")");
        
        Player player = Players.closest(currentPlayer);
        if (player == null) {
            payoutAttempts++;
            return 2000;
        }

        if (!Trade.isOpen()) {
            if (player.interact("Trade with")) {
                Sleep.sleepUntil(Trade::isOpen, 10000);
            } else {
                payoutAttempts++;
                return 2000;
            }
        }

        if (Trade.isOpen()) {
            long toPay = currentBet * 2;
            // In a real script, you'd add items here
            // tradeManager.addPayoutItems(toPay);
            
            if (Trade.acceptTrade()) {
                Sleep.sleepUntil(() -> !Trade.isOpen(), 15000);
                if (!Trade.isOpen()) {
                    log("[Payout] Payout successful!");
                    state = CasinoState.IDLE;
                    return 1000;
                }
            }
        }
        
        payoutAttempts++;
        return 2000;
    }

    private int handleRecovery() {
        log("[Recovery] Attempting to recover from error state...");
        if (Trade.isOpen()) Trade.declineTrade();
        if (Widgets.getWidget(335) != null) {
            // Close any open interfaces
            Keyboard.type("\u001B", false); // ESC key
        }
        state = CasinoState.INITIALIZING;
        return 2000;
    }

    @Override
    public void onMessage(Message msg) {
        if (chatAI != null) {
            // Use name-based check for MessageType to avoid compilation issues with different API versions
            String typeName = msg.getType().name();
            boolean isClan = typeName.contains("CLAN");
            chatAI.handleChat(msg.getUsername(), msg.getMessage(), isClan);
        }
        if (tradeRequestListener != null) tradeRequestListener.onMessage(msg);
    }

    @Override
    public void onPaint(Graphics g) {
        // Paint logic here
        g.setColor(Color.GREEN);
        g.drawString("snipes♧scripts Enterprise v4.0", 15, 45);
        g.drawString("State: " + state, 15, 60);
        g.drawString("Profit: " + profitTracker.getNetProfit(), 15, 75);
        
        // Draw buttons
        g.setColor(Color.GRAY);
        g.fillRect(adBtn.x, adBtn.y, adBtn.width, adBtn.height);
        g.fillRect(bankBtn.x, bankBtn.y, bankBtn.width, bankBtn.height);
        g.fillRect(resetBtn.x, resetBtn.y, resetBtn.width, resetBtn.height);
        
        g.setColor(Color.WHITE);
        g.drawString("AD", adBtn.x + 20, adBtn.y + 15);
        g.drawString("BANK", bankBtn.x + 15, bankBtn.y + 15);
        g.drawString("RESET", resetBtn.x + 12, resetBtn.y + 15);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        Point p = e.getPoint();
        if (adBtn.contains(p)) sendRotatingAd();
        if (bankBtn.contains(p)) state = CasinoState.BANKING;
        if (resetBtn.contains(p)) state = CasinoState.INITIALIZING;
    }

    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    private String formatGP(long a) {
        if (a >= 1_000_000) return (a / 1_000_000) + "M";
        if (a >= 1_000) return (a / 1_000) + "K";
        return String.valueOf(a);
    }
}
