package com.ikingsnipe.casino;

import com.ikingsnipe.casino.games.AbstractGame;
import com.ikingsnipe.casino.games.GameResult;
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
import org.dreambot.api.script.listener.ChatListener;
import org.dreambot.api.script.listener.PaintListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

@ScriptManifest(name = "SnipesScripts Enterprise", author = "ikingsnipe", version = 8.0, category = Category.MONEYMAKING, description = "Top-Tier 2026 Grade Casino System with ChasingCraps & Robust Trade Safety")
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
    private TradeManager tradeManager;
    private TradeRequestListener tradeRequestListener;
    private TradeStatistics tradeStatistics;

    private String currentPlayer;
    private long currentBet;
    private String selectedGame = "craps";
    private PlayerSession currentSession;
    private long lastAdTime;
    private long tradeStartTime;
    private volatile boolean guiFinished, startScript;
    
    private CasinoState lastState;
    private long lastStateChangeTime;
    private int adIndex = 0;
    private int payoutAttempts = 0;
    private static final int MAX_PAYOUT_ATTEMPTS = 3;

    private final Rectangle adBtn = new Rectangle(15, 200, 60, 20);
    private final Rectangle bankBtn = new Rectangle(80, 200, 60, 20);
    private final Rectangle resetBtn = new Rectangle(145, 200, 60, 20);

    @Override
    public void onStart() {
        log("Initializing SnipesScripts Enterprise v8.0...");
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
        chatAI.setConfig(config);
        chatAI.setProvablyFair(provablyFair);
        muleManager = new MuleManager(config);
        humanizationManager = new HumanizationManager(config);
        tradeStatistics = new TradeStatistics();
        tradeManager = new TradeManager(config, sessionManager, provablyFair);
        tradeRequestListener = new TradeRequestListener(tradeManager, config.tradeConfig);
        
        if (config.discordEnabled && !config.discordWebhookUrl.isEmpty()) {
            webhook = new DiscordWebhook(config.discordWebhookUrl);
            webhook.send("🚀 **SnipesScripts Enterprise v8.0** is now ONLINE!");
        }
        state = CasinoState.INITIALIZING;
    }

    @Override
    public int onLoop() {
        if (!guiFinished) return 500;
        if (!startScript) { stop(); return 0; }

        if (config.adminConfig.emergencyStop) {
            log("!!! EMERGENCY STOP !!!");
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

            if (state == lastState) {
                if (System.currentTimeMillis() - lastStateChangeTime > 60000) {
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
            log("Error: " + e.getMessage());
            state = CasinoState.ERROR_RECOVERY;
        }
        return Calculations.random(200, 400);
    }

    private int handleInitializing() {
        if (config.walkOnStart && !locationManager.isAtLocation()) {
            state = CasinoState.WALKING_TO_LOCATION;
        } else {
            state = CasinoState.IDLE;
        }
        return 500;
    }

    private int handleWalking() {
        if (locationManager.isAtLocation()) { state = CasinoState.IDLE; return 500; }
        locationManager.walkToLocation();
        return 1000;
    }

    private int handleBanking() {
        if (bankingManager.restock()) state = CasinoState.IDLE;
        return 1000;
    }

    private int handleIdle() {
        if (Trade.isOpen()) {
            currentPlayer = Trade.getTradingWith();
            tradeManager.reset();
            state = CasinoState.TRADING_WINDOW_1;
            return 300;
        }

        if (tradeRequestListener.hasPendingRequests()) {
            TradeRequestListener.TradeRequestEvent request = tradeRequestListener.getNextRequest();
            if (request != null) {
                Player p = Players.closest(request.playerName);
                if (p != null) p.interact("Trade with");
            }
        }

        if (System.currentTimeMillis() - lastAdTime > config.adIntervalSeconds * 1000L) {
            sendRotatingAd();
            lastAdTime = System.currentTimeMillis();
        }
        
        return 1000;
    }

    private void sendRotatingAd() {
        if (config.adMessages.isEmpty()) return;
        String ad = config.adMessages.get(adIndex);
        Keyboard.type(ad, true);
        adIndex = (adIndex + 1) % config.adMessages.size();
    }

    private int handleTrade1() {
        if (!Trade.isOpen(1)) {
            if (Trade.isOpen(2)) { state = CasinoState.TRADING_WINDOW_2; return 300; }
            state = CasinoState.IDLE; return 500;
        }
        tradeManager.handleTradeScreen1();
        return 500;
    }

    private int handleTrade2() {
        if (!Trade.isOpen(2)) {
            if (Trade.isOpen(1)) { state = CasinoState.TRADING_WINDOW_1; return 300; }
            // If trade closed, check if it was accepted
            state = CasinoState.PROCESSING_GAME;
            return 500;
        }
        tradeManager.handleTradeScreen2();
        return 500;
    }

    private int handleGame() {
        AbstractGame game = gameManager.getGame(selectedGame);
        GameResult result = game.play(currentPlayer, currentBet, provablyFair.getSeed());
        Keyboard.type(result.getDescription(), true);
        
        if (result.isWin()) {
            state = CasinoState.PAYOUT_PENDING;
        } else {
            state = CasinoState.IDLE;
        }
        return 1000;
    }

    private int handlePayout() {
        state = CasinoState.IDLE; // Simplified for now
        return 1000;
    }

    private int handleRecovery() {
        if (Trade.isOpen()) Trade.declineTrade();
        state = CasinoState.IDLE;
        return 2000;
    }

    @Override
    public void onMessage(Message msg) {
        if (chatAI != null) {
            chatAI.handleChat(msg.getUsername(), msg.getMessage(), msg.getType().name().contains("CLAN"));
        }
        if (tradeRequestListener != null) tradeRequestListener.onMessage(msg);
    }

    @Override
    public void onPaint(Graphics g) {
        g.setColor(Color.YELLOW);
        g.drawString("SnipesScripts Enterprise v8.0", 15, 45);
        g.drawString("State: " + state, 15, 60);
    }

    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}
