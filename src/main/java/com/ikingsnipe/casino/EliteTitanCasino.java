package com.ikingsnipe.casino;

import com.ikingsnipe.casino.core.CasinoState;
import com.ikingsnipe.casino.games.GameResult;
import com.ikingsnipe.casino.gui.CasinoGUI;
import com.ikingsnipe.casino.managers.*;
import com.ikingsnipe.casino.models.*;
import com.ikingsnipe.casino.utils.ProvablyFair;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.input.Keyboard;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.trade.Trade;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.items.Item;
import javax.swing.*;
import java.awt.*;

@ScriptManifest(
    name = "Elite Titan Casino Enterprise",
    description = "Enterprise-grade OSRS Casino Host Bot",
    author = "ikingsnipe",
    version = 13.0,
    category = Category.MISC
)
public class EliteTitanCasino extends AbstractScript {
    private CasinoConfig config;
    private CasinoState state = CasinoState.INITIALIZING;
    private CasinoGUI gui;
    private GameManager gameManager;
    private SessionManager sessionManager;
    private BankingManager bankingManager;
    private LocationManager locationManager;
    private ProvablyFair provablyFair;

    private String currentPlayer;
    private long currentBet;
    private PlayerSession currentSession;
    private long lastAdTime;
    private boolean welcomeSent;
    private volatile boolean guiFinished, startScript;

    @Override
    public void onStart() {
        config = new CasinoConfig();
        SwingUtilities.invokeLater(() -> {
            gui = new CasinoGUI(config, b -> {
                guiFinished = true;
                startScript = b;
                if (b) initSystems();
            });
            gui.setVisible(true);
        });
    }

    private void initSystems() {
        gameManager = new GameManager(config);
        sessionManager = new SessionManager( );
        bankingManager = new BankingManager(config);
        locationManager = new LocationManager(config);
        provablyFair = new ProvablyFair();
        state = CasinoState.INITIALIZING;
    }

    @Override
    public int onLoop() {
        if (!guiFinished) return 500;
        if (!startScript) { stop(); return 0; }

        try {
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
            state = CasinoState.ERROR_RECOVERY;
        }
        return Calculations.random(200, 400);
    }

    private int handleInitializing() {
        if (config.walkOnStart && !locationManager.isAtLocation()) {
            state = CasinoState.WALKING_TO_LOCATION;
        } else if (config.autoBank && getInventoryValue() < config.restockThreshold) {
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
        bankingManager.restock();
        state = CasinoState.INITIALIZING;
        return 1000;
    }

    private int handleIdle() {
        if (Trade.isOpen()) {
            currentPlayer = Trade.getTradingWith();
            if (currentPlayer != null) {
                currentSession = sessionManager.getSession(currentPlayer);
                provablyFair.generateNewSeed();
                state = CasinoState.TRADING_WINDOW_1;
                welcomeSent = false;
            }
            return 500;
        }
        if (System.currentTimeMillis() - lastAdTime > config.adIntervalSeconds * 1000L) {
            Keyboard.type(config.adMessage, true);
            lastAdTime = System.currentTimeMillis();
        }
        return 1000;
    }

    private int handleTrade1() {
        if (!Trade.isOpen(1)) { if (!Trade.isOpen()) reset(); return 500; }
        if (!welcomeSent) {
            Keyboard.type(String.format(config.tradeWelcome, provablyFair.getHash().substring(0, 12)), true);
            welcomeSent = true;
        }
        long val = getTradeValue();
        if (val >= config.minBet && val <= config.maxBet) {
            currentBet = val;
            if (Trade.acceptTrade()) Sleep.sleepUntil(() -> Trade.isOpen(2), 5000);
        }
        return 1000;
    }

    private int handleTrade2() {
        if (!Trade.isOpen(2)) { if (!Trade.isOpen()) state = CasinoState.PROCESSING_GAME; return 500; }
        if (Trade.acceptTrade()) Sleep.sleepUntil(() -> !Trade.isOpen(), 5000);
        return 1000;
    }

    private int handleGame() {
        GameResult r = gameManager.play(config.defaultGame, currentBet);
        String seed = provablyFair.getSeed().substring(0, 8);
        if (r.isWin()) {
            Keyboard.type(String.format(config.winMessage, currentPlayer, formatGP(r.getPayout()), r.getDescription(), seed), true);
            currentSession.setOwedAmount(r.getPayout());
            state = CasinoState.PAYOUT_PENDING;
        } else {
            Keyboard.type(String.format(config.lossMessage, currentPlayer, r.getDescription()), true);
            reset();
        }
        return 1000;
    }

    private int handlePayout() {
        if (currentSession.getOwedAmount() <= 0) { reset(); return 500; }
        Player p = Players.closest(currentPlayer);
        if (p == null) return 2000;
        if (Trade.isOpen()) {
            if (Trade.isOpen(1)) {
                addPayout(currentSession.getOwedAmount());
                Trade.acceptTrade();
            } else if (Trade.isOpen(2)) {
                if (Trade.acceptTrade()) { currentSession.setOwedAmount(0); reset(); }
            }
        } else {
            p.interact("Trade with");
            Sleep.sleepUntil(Trade::isOpen, 5000);
        }
        return 1000;
    }

    private int handleRecovery() { Widgets.closeAll(); reset(); return 1000; }

    private void reset() {
        state = CasinoState.IDLE; currentPlayer = null; currentBet = 0; welcomeSent = false;
    }

    private long getTradeValue() {
        long v = 0;
        Item[] items = Trade.getTheirItems();
        if (items != null) {
            for (Item i : items) {
                if (i == null) continue;
                if (i.getID() == CasinoConfig.COINS_ID) v += i.getAmount();
                else if (i.getID() == CasinoConfig.PLATINUM_TOKEN_ID) v += i.getAmount() * 1000L;
            }
        }
        return v;
    }

    private void addPayout(long amount) {
        long rem = amount;
        if (rem >= 1000) {
            int tokens = (int)(rem / 1000);
            Trade.addItem(CasinoConfig.PLATINUM_TOKEN_ID, tokens);
            rem %= 1000;
        }
        if (rem > 0) Trade.addItem(CasinoConfig.COINS_ID, (int)rem);
    }

    private long getInventoryValue() {
        return Inventory.count(CasinoConfig.COINS_ID) + Inventory.count(CasinoConfig.PLATINUM_TOKEN_ID) * 1000L;
    }

    private String formatGP(long a) {
        if (a >= 1_000_000) return (a / 1_000_000) + "M";
        if (a >= 1_000) return (a / 1_000) + "K";
        return String.valueOf(a);
    }

    @Override
    public void onPaint(Graphics g) {
        g.setColor(Color.YELLOW);
        g.drawString("Elite Casino Enterprise v13.0", 10, 50);
        g.drawString("Status: " + state.getStatus(), 10, 70);
        g.drawString("Inventory: " + formatGP(getInventoryValue()), 10, 90);
    }
}
